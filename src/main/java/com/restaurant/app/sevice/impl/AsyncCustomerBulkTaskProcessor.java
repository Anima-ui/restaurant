package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.sevice.CustomerService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncCustomerBulkTaskProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCustomerBulkTaskProcessor.class);

    private final AsyncCustomerBulkTaskRegistry taskRegistry;

    private final CustomerService customerService;

    @Value("${app.async.bulk.processing-delay-ms:2000}")
    private long processingDelayMillis;

    @Value("${app.async.bulk.enforce-min-delay:false}")
    private boolean enforceMinimumDelay;

    public AsyncCustomerBulkTaskProcessor(AsyncCustomerBulkTaskRegistry taskRegistry,
                                          CustomerService customerService) {
        this.taskRegistry = taskRegistry;
        this.customerService = customerService;
    }

    @Async("customerBulkTaskExecutor")
    public CompletableFuture<Void> processTask(Long taskId, CustomerBulkCreateRequest request) {
        taskRegistry.markRunning(taskId);
        LOGGER.info("Async customer bulk task {} started", taskId);
        try {
            CustomerBulkResult result = processCustomersOneByOne(taskId, request);
            taskRegistry.markCompleted(taskId, result);
            LOGGER.info("Async customer bulk task {} completed", taskId);
        } catch (RuntimeException exception) {
            taskRegistry.markFailed(taskId, exception.getMessage());
            LOGGER.warn("Async customer bulk task {} failed: {}", taskId, exception.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    private CustomerBulkResult processCustomersOneByOne(Long taskId, CustomerBulkCreateRequest request) {
        List<CustomerCreateRequest> customers = request.getCustomers();
        if (customers == null || customers.isEmpty()) {
            throw new IllegalArgumentException("Customer bulk request must contain at least one item");
        }

        int savedCount = 0;
        for (CustomerCreateRequest customerRequest : customers) {
            pauseBetweenItems();
            customerService.create(customerRequest);
            savedCount++;
            taskRegistry.updateProgress(
                    taskId,
                    savedCount,
                    "Processed " + savedCount + " of " + customers.size() + " customers"
            );
        }

        return CustomerBulkResult.builder()
                .scenario("ASYNC_CUSTOMER_BULK")
                .requestedCount(customers.size())
                .savedCount(savedCount)
                .note("Bulk operation completed successfully")
                .build();
    }

    private void pauseBetweenItems() {
        long effectiveDelayMillis = enforceMinimumDelay
                ? Math.max(processingDelayMillis, 1_000L)
                : processingDelayMillis;

        if (effectiveDelayMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(effectiveDelayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Async task delay was interrupted", exception);
        }
    }
}
