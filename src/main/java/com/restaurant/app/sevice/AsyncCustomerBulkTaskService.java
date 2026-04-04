package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.AsyncCustomerBulkTaskStatusDto;
import com.restaurant.app.domain.dto.AsyncTaskStartResponse;
import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;

public interface AsyncCustomerBulkTaskService {

    AsyncTaskStartResponse startBulkCreate(CustomerBulkCreateRequest request);

    AsyncCustomerBulkTaskStatusDto getTaskStatus(Long taskId);
}
