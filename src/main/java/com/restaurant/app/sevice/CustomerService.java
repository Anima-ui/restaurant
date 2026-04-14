package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.CustomerDto;

import java.util.List;

public interface CustomerService {
    CustomerDto create(CustomerCreateRequest request);

    CustomerBulkResult createBulk(CustomerBulkCreateRequest request);

    List<CustomerDto> getAll();

    CustomerDto getById(Long id);

    void delete(Long id);
}
