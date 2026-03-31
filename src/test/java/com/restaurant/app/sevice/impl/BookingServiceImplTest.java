package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.BookingCreateRequest;
import com.restaurant.app.domain.model.Booking;
import com.restaurant.app.domain.model.BookingStatus;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.exception.ResourceNotFoundException;
import com.restaurant.app.repository.BookingRepository;
import com.restaurant.app.repository.CustomerRepository;
import com.restaurant.app.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createSavesBookingWhenCustomerAndTableExist() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .bookingTime(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.CREATED)
                .customerId(1L)
                .tableId(2L)
                .build();
        Customer customer = Customer.builder().id(1L).fullName("Ivan").build();
        Restaurant restaurant = Restaurant.builder().id(10L).name("Roma").build();
        RestaurantTable table = RestaurantTable.builder()
                .id(2L)
                .tableNumber(7)
                .restaurant(restaurant)
                .build();
        Booking savedBooking = Booking.builder()
                .id(3L)
                .bookingTime(request.getBookingTime())
                .status(BookingStatus.CREATED)
                .customer(customer)
                .table(table)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantTableRepository.findById(2L)).thenReturn(Optional.of(table));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        assertThat(bookingService.create(request).getRestaurantName()).isEqualTo("Roma");
    }

    @Test
    void createThrowsWhenCustomerIsMissing() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .bookingTime(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.CREATED)
                .customerId(1L)
                .tableId(2L)
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer with id=1");
    }

    @Test
    void createThrowsWhenTableIsMissing() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .bookingTime(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.CREATED)
                .customerId(1L)
                .tableId(2L)
                .build();
        Customer customer = Customer.builder().id(1L).fullName("Ivan").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantTableRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant table with id=2");
    }

    @Test
    void getAllReturnsMappedBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking()));

        assertThat(bookingService.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsMappedBooking() {
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking()));

        assertThat(bookingService.getById(3L).getTableNumber()).isEqualTo(7);
    }

    @Test
    void getByIdThrowsWhenBookingIsMissing() {
        when(bookingRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(3L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking with id=3");
    }

    @Test
    void updateStatusChangesAndSavesBooking() {
        Booking booking = booking();
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        assertThat(bookingService.updateStatus(3L, BookingStatus.CONFIRMED).getStatus())
                .isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateStatusThrowsWhenBookingIsMissing() {
        when(bookingRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateStatus(3L, BookingStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking with id=3");
    }

    @Test
    void getAvailableStatusesReturnsAllEnumValues() {
        assertThat(bookingService.getAvailableStatuses())
                .containsExactly(BookingStatus.values());
    }

    private Booking booking() {
        Customer customer = Customer.builder().id(1L).fullName("Ivan").build();
        Restaurant restaurant = Restaurant.builder().id(10L).name("Roma").build();
        RestaurantTable table = RestaurantTable.builder()
                .id(2L)
                .tableNumber(7)
                .restaurant(restaurant)
                .build();
        return Booking.builder()
                .id(3L)
                .bookingTime(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.CREATED)
                .customer(customer)
                .table(table)
                .build();
    }
}
