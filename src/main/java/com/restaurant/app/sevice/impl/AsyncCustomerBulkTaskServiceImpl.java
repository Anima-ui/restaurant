package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.AsyncCustomerBulkTaskStatusDto;
import com.restaurant.app.domain.dto.AsyncTaskStartResponse;
import com.restaurant.app.domain.dto.AsyncTaskStatus;
import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.sevice.AsyncCustomerBulkTaskService;
import org.springframework.stereotype.Service;

@Service
public class AsyncCustomerBulkTaskServiceImpl implements AsyncCustomerBulkTaskService {

    private final AsyncCustomerBulkTaskRegistry taskRegistry;

    private final AsyncCustomerBulkTaskProcessor taskProcessor;

    public AsyncCustomerBulkTaskServiceImpl(AsyncCustomerBulkTaskRegistry taskRegistry,
                                            AsyncCustomerBulkTaskProcessor taskProcessor) {
        this.taskRegistry = taskRegistry;
        this.taskProcessor = taskProcessor;
    }

    public AsyncTaskStartResponse startBulkCreate(CustomerBulkCreateRequest request) {
        int requestedCount = request.getCustomers() == null ? 0 : request.getCustomers().size();
        Long taskId = taskRegistry.createTask(requestedCount);
        taskProcessor.processTask(taskId, request);
        return AsyncTaskStartResponse.builder()
                .taskId(taskId)
                .status(AsyncTaskStatus.PENDING)
                .message("Async customer bulk task has been accepted")
                .build();
    }

    public AsyncCustomerBulkTaskStatusDto getTaskStatus(Long taskId) {
        return taskRegistry.getTaskStatus(taskId);
    }
}
