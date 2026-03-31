package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.CustomerDto;
import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.TransactionDemoResult;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.exception.ConflictOperationException;
import com.restaurant.app.repository.CustomerRepository;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.repository.RestaurantTableRepository;
import com.restaurant.app.sevice.TransactionDemoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TransactionDemoServiceImpl implements TransactionDemoService {

    private final RestaurantRepository restaurantRepository;

    private final RestaurantTableRepository tableRepository;

    private final CustomerRepository customerRepository;

    public TransactionDemoServiceImpl(RestaurantRepository restaurantRepository,
                                      RestaurantTableRepository tableRepository,
                                      CustomerRepository customerRepository) {
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
        this.customerRepository = customerRepository;
    }

    public TransactionDemoResult savePartiallyWithoutTransaction(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (WITHOUT_TRANSACTION)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(1)
                .seats(2)
                .restaurant(savedRestaurant)
                .build();
        tableRepository.save(table);

        try {
            throw new IllegalStateException("Simulated failure after save without transaction");
        } catch (RuntimeException exception) {
            return TransactionDemoResult.builder()
                    .scenario("WITHOUT_TRANSACTION")
                    .restaurantsInDb(restaurantRepository.count())
                    .tablesInDb(tableRepository.count())
                    .note("Exception happened, data remained committed without transaction.")
                    .build();
        }
    }

    @Transactional
    public TransactionDemoResult rollbackCompletelyWithTransaction(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (WITH_TRANSACTION)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(99)
                .seats(4)
                .restaurant(savedRestaurant)
                .build();
        tableRepository.save(table);

        throw new ConflictOperationException("Simulated failure to trigger rollback");
    }

    @Transactional
    public TransactionDemoResult saveWithCascade(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (WITH_CASCADE)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        RestaurantTable table1 = RestaurantTable.builder()
                .tableNumber(101)
                .seats(2)
                .restaurant(restaurant)
                .build();
        RestaurantTable table2 = RestaurantTable.builder()
                .tableNumber(102)
                .seats(4)
                .restaurant(restaurant)
                .build();

        restaurant.getTables().add(table1);
        restaurant.getTables().add(table2);
        restaurantRepository.save(restaurant);

        return TransactionDemoResult.builder()
                .scenario("CASCADE_SAVE")
                .restaurantsInDb(restaurantRepository.count())
                .tablesInDb(tableRepository.count())
                .note("Restaurant and tables were saved via cascade.")
                .build();
    }

    public TransactionDemoResult saveRestaurantAndThrowException(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (EXCEPTION_NO_TX)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        restaurantRepository.save(restaurant);
        throw new ConflictOperationException("Exception after save without transaction");
    }

    @Transactional
    public TransactionDemoResult saveRestaurantAndThrowExceptionWithTransactional(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (EXCEPTION_WITH_TX)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        restaurantRepository.save(restaurant);
        throw new ConflictOperationException("Exception after save with transaction");
    }

    public TransactionDemoResult getCurrentState(String scenario, String note) {
        return TransactionDemoResult.builder()
                .scenario(scenario)
                .restaurantsInDb(restaurantRepository.count())
                .tablesInDb(tableRepository.count())
                .note(note)
                .build();
    }

    public CustomerBulkResult bulkCreateCustomersWithoutTransaction(CustomerBulkCreateRequest request) {
        List<CustomerCreateRequest> customers = getCustomersFromBulkRequest(request);
        List<CustomerDto> savedCustomers = new ArrayList<>();
        Set<String> processedPhones = new LinkedHashSet<>();

        try {
            for (CustomerCreateRequest customerRequest : customers) {
                if (!processedPhones.add(customerRequest.getPhone())
                        || customerRepository.findByPhone(customerRequest.getPhone()).isPresent()) {
                    throw new ConflictOperationException(
                            "Bulk demo conflict for phone=" + customerRequest.getPhone()
                    );
                }
                savedCustomers.add(saveCustomer(customerRequest));
            }
        } catch (RuntimeException exception) {
            return CustomerBulkResult.builder()
                    .scenario("CUSTOMER_BULK_WITHOUT_TX")
                    .requestedCount(customers.size())
                    .savedCount(savedCustomers.size())
                    .customersInDb(customerRepository.count())
                    .savedCustomers(savedCustomers)
                    .note("Exception happened, already saved customers remained in DB without transaction")
                    .build();
        }

        return CustomerBulkResult.builder()
                .scenario("CUSTOMER_BULK_WITHOUT_TX")
                .requestedCount(customers.size())
                .savedCount(savedCustomers.size())
                .customersInDb(customerRepository.count())
                .savedCustomers(savedCustomers)
                .note("Bulk demo completed without exception")
                .build();
    }

    @Transactional
    public CustomerBulkResult bulkCreateCustomersWithTransaction(CustomerBulkCreateRequest request) {
        List<CustomerCreateRequest> customers = getCustomersFromBulkRequest(request);
        Set<String> processedPhones = new LinkedHashSet<>();

        customers.forEach(customerRequest -> {
            if (!processedPhones.add(customerRequest.getPhone())
                    || customerRepository.findByPhone(customerRequest.getPhone()).isPresent()) {
                throw new ConflictOperationException(
                        "Bulk demo conflict for phone=" + customerRequest.getPhone()
                );
            }
            saveCustomer(customerRequest);
        });

        return CustomerBulkResult.builder()
                .scenario("CUSTOMER_BULK_WITH_TX")
                .requestedCount(customers.size())
                .savedCount(customers.size())
                .customersInDb(customerRepository.count())
                .note("Bulk demo completed in transaction")
                .build();
    }

    private List<CustomerCreateRequest> getCustomersFromBulkRequest(CustomerBulkCreateRequest request) {
        return Optional
                .ofNullable(request.getCustomers())
                .filter(customers -> !customers.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer bulk request must contain at least one item"
                ));
    }

    private CustomerDto saveCustomer(CustomerCreateRequest request) {
        Customer savedCustomer = customerRepository.save(Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build());

        return CustomerDto.builder()
                .id(savedCustomer.getId())
                .fullName(savedCustomer.getFullName())
                .phone(savedCustomer.getPhone())
                .build();
    }
}
