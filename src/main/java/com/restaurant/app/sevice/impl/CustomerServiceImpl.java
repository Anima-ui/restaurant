package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerDto;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.repository.CustomerRepository;
import com.restaurant.app.sevice.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerDto create(CustomerCreateRequest request) {
        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();
        return toDto(customerRepository.save(customer));
    }

    public List<CustomerDto> getAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    public CustomerDto getById(Long id) {
        return toDto(customerRepository.findById(id).orElseThrow());
    }

    private CustomerDto toDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .build();
    }
}
