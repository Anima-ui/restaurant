package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.exception.ConflictOperationException;
import com.restaurant.app.sevice.CustomerService;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncCustomerBulkTaskProcessorTest {

    @Mock
    private AsyncCustomerBulkTaskRegistry taskRegistry;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AsyncCustomerBulkTaskProcessor taskProcessor;

    @Test
    void processTaskMarksTaskCompletedWhenBulkCreateSucceeds() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(java.util.List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001123").build()
                ))
                .build();

        ReflectionTestUtils.setField(taskProcessor, "processingDelayMillis", 1L);
        when(customerService.create(request.getCustomers().get(0)))
                .thenReturn(com.restaurant.app.domain.dto.CustomerDto.builder().id(1L).build());
        when(customerService.create(request.getCustomers().get(1)))
                .thenReturn(com.restaurant.app.domain.dto.CustomerDto.builder().id(2L).build());

        CompletableFuture<Void> future = taskProcessor.processTask(1L, request);

        assertThat(future).isCompleted();
        verify(taskRegistry).markRunning(1L);
        verify(taskRegistry).updateProgress(1L, 1, "Processed 1 of 2 customers");
        verify(taskRegistry).updateProgress(1L, 2, "Processed 2 of 2 customers");
        verify(taskRegistry).markCompleted(1L, CustomerBulkResult.builder()
                .scenario("ASYNC_CUSTOMER_BULK")
                .requestedCount(2)
                .savedCount(2)
                .note("Bulk operation completed successfully")
                .build());
    }

    @Test
    void processTaskMarksTaskFailedWhenBulkCreateThrowsException() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(java.util.List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build()
                ))
                .build();

        ReflectionTestUtils.setField(taskProcessor, "processingDelayMillis", 1L);
        when(customerService.create(request.getCustomers().get(0)))
                .thenThrow(new ConflictOperationException("Duplicate phones in bulk request"));

        CompletableFuture<Void> future = taskProcessor.processTask(1L, request);

        assertThat(future).isCompleted();
        verify(taskRegistry).markRunning(1L);
        verify(taskRegistry).markFailed(1L, "Duplicate phones in bulk request");
    }

    @Test
    void processTaskSkipsDelayWhenItIsDisabled() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(java.util.List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build()
                ))
                .build();

        ReflectionTestUtils.setField(taskProcessor, "processingDelayMillis", 0L);
        when(customerService.create(request.getCustomers().get(0)))
                .thenReturn(com.restaurant.app.domain.dto.CustomerDto.builder().id(1L).build());

        CompletableFuture<Void> future = taskProcessor.processTask(5L, request);

        assertThat(future).isCompleted();
        verify(taskRegistry).markRunning(5L);
        verify(taskRegistry).updateProgress(5L, 1, "Processed 1 of 1 customers");
        verify(taskRegistry).markCompleted(5L, CustomerBulkResult.builder()
                .scenario("ASYNC_CUSTOMER_BULK")
                .requestedCount(1)
                .savedCount(1)
                .note("Bulk operation completed successfully")
                .build());
    }

    @Test
    void processTaskMarksTaskFailedWhenDelayIsInterrupted() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(java.util.List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build()
                ))
                .build();

        ReflectionTestUtils.setField(taskProcessor, "processingDelayMillis", 1L);
        ReflectionTestUtils.setField(taskProcessor, "enforceMinimumDelay", true);
        Thread.currentThread().interrupt();

        try {
            CompletableFuture<Void> future = taskProcessor.processTask(1L, request);

            assertThat(future).isCompleted();
            verify(taskRegistry).markRunning(1L);
            verify(taskRegistry).markFailed(1L, "Async task delay was interrupted");
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void processTaskFailsWhenCustomerListIsMissing() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder().build();

        ReflectionTestUtils.setField(taskProcessor, "processingDelayMillis", 0L);

        CompletableFuture<Void> future = taskProcessor.processTask(3L, request);

        assertThat(future).isCompleted();
        verify(taskRegistry).markRunning(3L);
        verify(taskRegistry).markFailed(3L, "Customer bulk request must contain at least one item");
    }

    @Test
    void processTaskFailsWhenCustomerListIsEmpty() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(java.util.List.of())
                .build();

        ReflectionTestUtils.setField(taskProcessor, "processingDelayMillis", 0L);

        CompletableFuture<Void> future = taskProcessor.processTask(4L, request);

        assertThat(future).isCompleted();
        verify(taskRegistry).markRunning(4L);
        verify(taskRegistry).markFailed(4L, "Customer bulk request must contain at least one item");
    }
}
