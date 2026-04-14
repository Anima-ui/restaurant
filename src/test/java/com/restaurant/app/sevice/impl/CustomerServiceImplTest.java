package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerCreateRequest;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.exception.ConflictOperationException;
import com.restaurant.app.exception.ResourceNotFoundException;
import com.restaurant.app.repository.BookingRepository;
import com.restaurant.app.repository.CustomerRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createSavesCustomerWhenPhoneIsUnique() {
        CustomerCreateRequest request = CustomerCreateRequest.builder()
                .fullName("Ivan Petrov")
                .phone("+79990001122")
                .build();
        Customer savedCustomer = Customer.builder()
                .id(1L)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        when(customerRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        assertThat(customerService.create(request).getId()).isEqualTo(1L);
    }

    @Test
    void createThrowsConflictWhenPhoneAlreadyExists() {
        CustomerCreateRequest request = CustomerCreateRequest.builder()
                .fullName("Ivan Petrov")
                .phone("+79990001122")
                .build();

        when(customerRepository.findByPhone(request.getPhone()))
                .thenReturn(Optional.of(Customer.builder().id(1L).phone(request.getPhone()).build()));

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining(request.getPhone());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createBulkSavesAllCustomersWhenPhonesAreUnique() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001123").build()
                ))
                .build();
        List<Customer> savedCustomers = List.of(
                Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build(),
                Customer.builder().id(2L).fullName("Petr").phone("+79990001123").build()
        );

        when(customerRepository.findAllByPhoneIn(List.of("+79990001122", "+79990001123"))).thenReturn(List.of());
        when(customerRepository.saveAll(any())).thenReturn(savedCustomers);
        when(customerRepository.count()).thenReturn(2L);

        assertThat(customerService.createBulk(request).getSavedCount()).isEqualTo(2);
    }

    @Test
    void createBulkThrowsConflictWhenRequestContainsDuplicatePhones() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build(),
                        CustomerCreateRequest.builder().fullName("Petr").phone("+79990001122").build()
                ))
                .build();

        assertThatThrownBy(() -> customerService.createBulk(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("+79990001122");
    }

    @Test
    void createBulkThrowsConflictWhenPhoneAlreadyExistsInDatabase() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of(
                        CustomerCreateRequest.builder().fullName("Ivan").phone("+79990001122").build()
                ))
                .build();

        when(customerRepository.findAllByPhoneIn(List.of("+79990001122")))
                .thenReturn(List.of(Customer.builder().id(10L).phone("+79990001122").build()));

        assertThatThrownBy(() -> customerService.createBulk(request))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("+79990001122");
    }

    @Test
    void createBulkThrowsWhenRequestIsEmpty() {
        CustomerBulkCreateRequest request = CustomerBulkCreateRequest.builder()
                .customers(List.of())
                .build();

        assertThatThrownBy(() -> customerService.createBulk(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void getAllReturnsMappedCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(
                Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build()
        ));

        assertThat(customerService.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsMappedCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(
                Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build()
        ));

        assertThat(customerService.getById(1L).getFullName()).isEqualTo("Ivan");
    }

    @Test
    void getByIdThrowsWhenCustomerIsMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer with id=1");
    }

    @Test
    void deleteRemovesCustomerWhenNoBookingsExist() {
        Customer customer = Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(bookingRepository.existsByCustomerId(1L)).thenReturn(false);

        customerService.delete(1L);

        verify(customerRepository).delete(customer);
    }

    @Test
    void deleteThrowsConflictWhenCustomerHasBookings() {
        Customer customer = Customer.builder().id(1L).fullName("Ivan").phone("+79990001122").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(bookingRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.delete(1L))
                .isInstanceOf(ConflictOperationException.class)
                .hasMessageContaining("bookings are linked");
    }

    @Test
    void deleteThrowsWhenCustomerIsMissing() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer with id=1");
    }
}
