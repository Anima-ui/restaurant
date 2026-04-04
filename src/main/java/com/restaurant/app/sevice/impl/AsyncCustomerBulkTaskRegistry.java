package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.AsyncCustomerBulkTaskStatusDto;
import com.restaurant.app.domain.dto.AsyncTaskStatus;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class AsyncCustomerBulkTaskRegistry {

    private final AtomicLong taskIdGenerator = new AtomicLong();

    private final Map<Long, AsyncCustomerBulkTaskStatusDto> tasks = new ConcurrentHashMap<>();

    public Long createTask(int requestedCount) {
        long taskId = taskIdGenerator.incrementAndGet();
        tasks.put(taskId, AsyncCustomerBulkTaskStatusDto.builder()
                .taskId(taskId)
                .status(AsyncTaskStatus.PENDING)
                .requestedCount(requestedCount)
                .savedCount(0)
                .note("Task is waiting in async queue")
                .createdAt(OffsetDateTime.now())
                .build());
        return taskId;
    }

    public AsyncCustomerBulkTaskStatusDto getTaskStatus(Long taskId) {
        return getExistingTask(taskId);
    }

    public void markRunning(Long taskId) {
        AsyncCustomerBulkTaskStatusDto task = getExistingTask(taskId);
        tasks.put(taskId, AsyncCustomerBulkTaskStatusDto.builder()
                .taskId(task.getTaskId())
                .status(AsyncTaskStatus.RUNNING)
                .requestedCount(task.getRequestedCount())
                .savedCount(task.getSavedCount())
                .note("Task is being processed")
                .errorMessage(null)
                .createdAt(task.getCreatedAt())
                .startedAt(OffsetDateTime.now())
                .completedAt(null)
                .build());
    }

    public void markCompleted(Long taskId, CustomerBulkResult result) {
        AsyncCustomerBulkTaskStatusDto task = getExistingTask(taskId);
        tasks.put(taskId, AsyncCustomerBulkTaskStatusDto.builder()
                .taskId(task.getTaskId())
                .status(AsyncTaskStatus.COMPLETED)
                .requestedCount(task.getRequestedCount())
                .savedCount(result.getSavedCount())
                .note(result.getNote())
                .errorMessage(null)
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(OffsetDateTime.now())
                .build());
    }

    public void updateProgress(Long taskId, int savedCount, String note) {
        AsyncCustomerBulkTaskStatusDto task = getExistingTask(taskId);
        tasks.put(taskId, AsyncCustomerBulkTaskStatusDto.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .requestedCount(task.getRequestedCount())
                .savedCount(savedCount)
                .note(note)
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .build());
    }

    public void markFailed(Long taskId, String errorMessage) {
        AsyncCustomerBulkTaskStatusDto task = getExistingTask(taskId);
        tasks.put(taskId, AsyncCustomerBulkTaskStatusDto.builder()
                .taskId(task.getTaskId())
                .status(AsyncTaskStatus.FAILED)
                .requestedCount(task.getRequestedCount())
                .savedCount(task.getSavedCount())
                .note("Task failed")
                .errorMessage(errorMessage)
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(OffsetDateTime.now())
                .build());
    }

    private AsyncCustomerBulkTaskStatusDto getExistingTask(Long taskId) {
        AsyncCustomerBulkTaskStatusDto task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Async task with id=" + taskId + " was not found");
        }
        return task;
    }
}
