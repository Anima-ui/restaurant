package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.AsyncTaskStatus;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsyncCustomerBulkTaskRegistryTest {

    private final AsyncCustomerBulkTaskRegistry registry = new AsyncCustomerBulkTaskRegistry();

    @Test
    void createTaskCreatesPendingTask() {
        Long taskId = registry.createTask(2);

        assertThat(registry.getTaskStatus(taskId))
                .extracting("taskId", "status", "requestedCount", "savedCount", "note")
                .containsExactly(taskId, AsyncTaskStatus.PENDING, 2, 0, "Task is waiting in async queue");
    }

    @Test
    void markRunningUpdatesTaskStatus() {
        Long taskId = registry.createTask(3);

        registry.markRunning(taskId);

        assertThat(registry.getTaskStatus(taskId))
                .extracting("status", "requestedCount", "startedAt", "completedAt")
                .containsExactly(AsyncTaskStatus.RUNNING, 3, registry.getTaskStatus(taskId).getStartedAt(), null);
    }

    @Test
    void markCompletedUpdatesTaskStatus() {
        Long taskId = registry.createTask(2);
        CustomerBulkResult result = CustomerBulkResult.builder()
                .savedCount(2)
                .note("Bulk operation completed successfully")
                .build();

        registry.markRunning(taskId);
        registry.markCompleted(taskId, result);

        assertThat(registry.getTaskStatus(taskId))
                .extracting("status", "savedCount", "note")
                .containsExactly(AsyncTaskStatus.COMPLETED, 2, "Bulk operation completed successfully");
    }

    @Test
    void updateProgressKeepsRunningStatusAndUpdatesSavedCount() {
        Long taskId = registry.createTask(4);

        registry.markRunning(taskId);
        registry.updateProgress(taskId, 2, "Processed 2 of 4 customers");

        assertThat(registry.getTaskStatus(taskId))
                .extracting("status", "requestedCount", "savedCount", "note")
                .containsExactly(AsyncTaskStatus.RUNNING, 4, 2, "Processed 2 of 4 customers");
    }

    @Test
    void markFailedUpdatesTaskStatus() {
        Long taskId = registry.createTask(1);

        registry.markRunning(taskId);
        registry.markFailed(taskId, "Customers already exist");

        assertThat(registry.getTaskStatus(taskId))
                .extracting("status", "errorMessage", "note")
                .containsExactly(AsyncTaskStatus.FAILED, "Customers already exist", "Task failed");
    }

    @Test
    void getTaskStatusThrowsWhenTaskIsMissing() {
        assertThatThrownBy(() -> registry.getTaskStatus(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateProgressThrowsWhenTaskIsMissing() {
        assertThatThrownBy(() -> registry.updateProgress(999L, 1, "note"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
