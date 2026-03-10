package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerDto;

import java.util.List;

public interface CustomerService {
    CustomerDto create(CustomerCreateRequest request);

    List<CustomerDto> getAll();

    CustomerDto getById(Long id);
}
