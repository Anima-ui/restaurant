package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.AsyncCustomerBulkTaskStatusDto;
import com.restaurant.app.domain.dto.AsyncTaskStatus;
import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncCustomerBulkTaskServiceImplTest {

    @Mock
    private AsyncCustomerBulkTaskRegistry taskRegistry;

    @Mock
    private AsyncCustomerBulkTaskProcessor taskProcessor;

    @InjectMocks
    private AsyncCustomerBulkTaskServiceImpl asyncTaskService;

    @Test
    void startBulkCreateReturnsPendingTaskResponse() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001123").build()
                ))
                .build();

        when(taskRegistry.createTask(2)).thenReturn(1L);

        assertThat(asyncTaskService.startBulkCreate(request))
                .extracting("taskId", "status", "message")
                .containsExactly(1L, AsyncTaskStatus.PENDING, "Async customer bulk task has been accepted");

        verify(taskProcessor).processTask(1L, request);
    }

    @Test
    void startBulkCreateUsesZeroWhenCustomerListIsNull() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder().build();

        when(taskRegistry.createTask(0)).thenReturn(7L);

        assertThat(asyncTaskService.startBulkCreate(request).getTaskId()).isEqualTo(7L);
        verify(taskProcessor).processTask(7L, request);
    }

    @Test
    void getTaskStatusDelegatesToRegistry() {
        AsyncCustomerBulkTaskStatusDto status = AsyncCustomerBulkTaskStatusDto.builder()
                .taskId(5L)
                .status(AsyncTaskStatus.RUNNING)
                .build();

        when(taskRegistry.getTaskStatus(5L)).thenReturn(status);

        assertThat(asyncTaskService.getTaskStatus(5L)).isSameAs(status);
    }
}
