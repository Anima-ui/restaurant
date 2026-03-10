package com.restaurant.app.controller.customer;

import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerDto;
import com.restaurant.app.sevice.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }
}
