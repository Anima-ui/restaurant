package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.exception.ConflictOperationException;
import com.restaurant.app.repository.CustomerRepository;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDemoServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionDemoServiceImpl transactionDemoService;

    @Test
    void savePartiallyWithoutTransactionReturnsStateAfterFailure() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();
        Restaurant savedRestaurant = Restaurant.builder().id(1L).name("Roma").build();

        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(savedRestaurant);
        when(tableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantRepository.count()).thenReturn(1L);
        when(tableRepository.count()).thenReturn(1L);

        assertThat(transactionDemoService.savePartiallyWithoutTransaction(request).getTablesInDb()).isEqualTo(1L);
    }

    @Test
    void rollbackCompletelyWithTransactionThrowsConflict() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();

        assertThatThrownBy(() -> transactionDemoService.rollbackCompletelyWithTransaction(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("rollback");
    }

    @Test
    void saveWithCascadeReturnsCurrentState() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();

        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restaurantRepository.count()).thenReturn(1L);
        when(tableRepository.count()).thenReturn(2L);

        assertThat(transactionDemoService.saveWithCascade(request).getTablesInDb()).isEqualTo(2L);
    }

    @Test
    void saveRestaurantAndThrowExceptionThrowsConflict() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();

        assertThatThrownBy(() -> transactionDemoService.saveRestaurantAndThrowException(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("without transaction");
    }

    @Test
    void saveRestaurantAndThrowExceptionWithTransactionalThrowsConflict() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();

        assertThatThrownBy(() -> transactionDemoService.saveRestaurantAndThrowExceptionWithTransactional(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("with transaction");
    }

    @Test
    void getCurrentStateReturnsCounts() {
        when(restaurantRepository.count()).thenReturn(3L);
        when(tableRepository.count()).thenReturn(7L);

        assertThat(transactionDemoService.getCurrentState("CURRENT", "note").getRestaurantsInDb()).isEqualTo(3L);
    }

    @Test
    void bulkCreateCustomersWithoutTransactionKeepsAlreadySavedCustomers() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001122").build()
                ))
                .build();

        when(customerRepository.findByPhone("+79990001122"))
                .thenReturn(Optional.empty(), Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build());
        when(customerRepository.count()).thenReturn(1L);

        assertThat(transactionDemoService.bulkCreateCustomersWithoutTransaction(request).getSavedCount()).isEqualTo(1);
    }

    @Test
    void bulkCreateCustomersWithoutTransactionReturnsSuccessWhenAllPhonesAreUnique() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001123").build()
                ))
                .build();

        when(customerRepository.findByPhone("+79990001122")).thenReturn(Optional.empty());
        when(customerRepository.findByPhone("+79990001123")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(
                        Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build(),
                        Customer.builder().id(2L).fullName("Petr").phone("+79990001123").build()
                );
        when(customerRepository.count()).thenReturn(2L);

        assertThat(transactionDemoService.bulkCreateCustomersWithoutTransaction(request).getSavedCount())
                .isEqualTo(2);
    }

    @Test
    void bulkCreateCustomersWithTransactionThrowsConflictOnDuplicatePhone() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001122").build()
                ))
                .build();

        when(customerRepository.findByPhone("+79990001122"))
                .thenReturn(Optional.empty(), Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build());

        assertThatThrownBy(() -> transactionDemoService.bulkCreateCustomersWithTransaction(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("+79990001122");
    }

    @Test
    void bulkCreateCustomersWithTransactionReturnsSuccessWhenAllPhonesAreUnique() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001123").build()
                ))
                .build();

        when(customerRepository.findByPhone("+79990001122")).thenReturn(Optional.empty());
        when(customerRepository.findByPhone("+79990001123")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(
                        Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build(),
                        Customer.builder().id(2L).fullName("Petr").phone("+79990001123").build()
                );
        when(customerRepository.count()).thenReturn(2L);

        assertThat(transactionDemoService.bulkCreateCustomersWithTransaction(request).getSavedCount())
                .isEqualTo(2);
    }

    @Test
    void bulkCreateCustomersWithTransactionThrowsWhenRequestIsEmpty() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of())
                .build();

        assertThatThrownBy(() -> transactionDemoService.bulkCreateCustomersWithTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }
}
