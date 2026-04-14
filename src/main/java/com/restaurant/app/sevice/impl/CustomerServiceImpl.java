package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerDto;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.exception.ConflictOperationException;
import com.restaurant.app.exception.ResourceNotFoundException;
import com.restaurant.app.repository.BookingRepository;
import com.restaurant.app.repository.CustomerRepository;
import com.restaurant.app.sevice.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String CUSTOMER_NOT_FOUND_PREFIX = "Customer with id=";

    private static final String NOT_FOUND_SUFFIX = " was not found";

    private static final String CUSTOMER_DELETE_CONFLICT_MESSAGE =
            "Customer cannot be deleted because bookings are linked to this customer";

    private final CustomerRepository customerRepository;

    private final BookingRepository bookingRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               BookingRepository bookingRepository) {
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public CustomerDto create(CustomerCreateRequest request) {
        customerRepository.findByPhone(request.getPhone())
                .ifPresent(customer -> {
                    String message = "Customer with phone="
                            + request.getPhone()
                            + " already exists";
                    throw new ConflictOperationException(message);
                });

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();
        return toDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerBulkResult createBulk(CustomerBulkCreateRequest request) {
        List<CustomerCreateRequest> customersToCreate = Optional
                .ofNullable(request.getCustomers())
                .filter(customers -> !customers.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer bulk request must contain at least one item"
                ));

        ensureNoDuplicatePhonesInsideRequest(customersToCreate);
        ensurePhonesDoNotExist(customersToCreate);

        List<CustomerDto> savedCustomers = customerRepository.saveAll(
                        customersToCreate.stream()
                                .map(this::toEntity)
                                .toList()
                ).stream()
                .map(this::toDto)
                .toList();

        return CustomerBulkResult.builder()
                .scenario("CUSTOMER_BULK")
                .requestedCount(customersToCreate.size())
                .savedCount(savedCustomers.size())
                .customersInDb(customerRepository.count())
                .savedCustomers(savedCustomers)
                .note("Bulk operation completed successfully")
                .build();
    }

    public List<CustomerDto> getAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    public CustomerDto getById(Long id) {
        return toDto(customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX)));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX));

        if (bookingRepository.existsByCustomerId(id)) {
            throw new ConflictOperationException(CUSTOMER_DELETE_CONFLICT_MESSAGE);
        }

        customerRepository.delete(customer);
    }

    private CustomerDto toDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .build();
    }

    private Customer toEntity(CustomerCreateRequest request) {
        return Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();
    }

    private void ensureNoDuplicatePhonesInsideRequest(List<CustomerCreateRequest> customersToCreate) {
        Set<String> uniquePhones = new LinkedHashSet<>();
        List<String> duplicatePhones = customersToCreate.stream()
                .map(CustomerCreateRequest::getPhone)
                .filter(phone -> !uniquePhones.add(phone))
                .distinct()
                .toList();

        if (!duplicatePhones.isEmpty()) {
            throw new ConflictOperationException("Duplicate phones in bulk request: " + duplicatePhones);
        }
    }

    private void ensurePhonesDoNotExist(List<CustomerCreateRequest> customersToCreate) {
        List<String> phones = customersToCreate.stream()
                .map(CustomerCreateRequest::getPhone)
                .toList();

        Map<String, Customer> existingCustomersByPhone = customerRepository.findAllByPhoneIn(phones).stream()
                .collect(Collectors.toMap(Customer::getPhone, Function.identity()));

        if (!existingCustomersByPhone.isEmpty()) {
            throw new ConflictOperationException(
                    "Customers already exist for phones: " + existingCustomersByPhone.keySet()
            );
        }
    }
}
