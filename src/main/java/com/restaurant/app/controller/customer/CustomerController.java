package com.restaurant.app.controller.customer;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerDto;
import com.restaurant.app.domain.dto.AsyncCustomerBulkTaskStatusDto;
import com.restaurant.app.domain.dto.AsyncTaskStartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.restaurant.app.sevice.AsyncCustomerBulkTaskService;
import com.restaurant.app.sevice.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@Validated
@Tag(name = "Customers", description = "Customer operations")
public class CustomerController {

    private final CustomerService customerService;

    private final AsyncCustomerBulkTaskService asyncCustomerBulkTaskService;

    public CustomerController(CustomerService customerService,
                              AsyncCustomerBulkTaskService asyncCustomerBulkTaskService) {
        this.customerService = customerService;
        this.asyncCustomerBulkTaskService = asyncCustomerBulkTaskService;
    }

    @PostMapping
    @Operation(summary = "Create customer")
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create customers in bulk")
    public ResponseEntity<CustomerBulkResult> createBulk(@Valid @RequestBody CustomerBulkCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createBulk(request));
    }

    @PostMapping("/bulk/async")
    @Operation(summary = "Start async bulk customer creation")
    public ResponseEntity<AsyncTaskStartResponse> createBulkAsync(
            @Valid @RequestBody CustomerBulkCreateRequest request) {
        return ResponseEntity.accepted().body(asyncCustomerBulkTaskService.startBulkCreate(request));
    }

    @GetMapping("/bulk/tasks/{taskId}")
    @Operation(summary = "Get async bulk customer task status")
    public ResponseEntity<AsyncCustomerBulkTaskStatusDto> getBulkTaskStatus(@PathVariable @Positive Long taskId) {
        return ResponseEntity.ok(asyncCustomerBulkTaskService.getTaskStatus(taskId));
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<CustomerDto>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by id")
    public ResponseEntity<CustomerDto> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer by id")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
