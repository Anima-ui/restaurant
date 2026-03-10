package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.BookingCreateRequest;
import com.restaurant.app.domain.dto.BookingDto;
import com.restaurant.app.domain.model.Booking;
import com.restaurant.app.domain.model.BookingStatus;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.repository.BookingRepository;
import com.restaurant.app.repository.CustomerRepository;
import com.restaurant.app.repository.RestaurantTableRepository;
import com.restaurant.app.sevice.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final CustomerRepository customerRepository;

    private final RestaurantTableRepository restaurantTableRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              CustomerRepository customerRepository,
                              RestaurantTableRepository restaurantTableRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Transactional
    public BookingDto create(BookingCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId()).orElseThrow();
        RestaurantTable table = restaurantTableRepository.findById(request.getTableId()).orElseThrow();

        Booking booking = Booking.builder()
                .bookingTime(request.getBookingTime())
                .status(request.getStatus())
                .customer(customer)
                .table(table)
                .build();

        return toDto(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getAll() {
        return bookingRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BookingDto getById(Long id) {
        return toDto(bookingRepository.findById(id).orElseThrow());
    }

    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(status);
        return toDto(bookingRepository.save(booking));
    }

    public List<BookingStatus> getAvailableStatuses() {
        return Arrays.stream(BookingStatus.values()).toList();
    }

    private BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFullName())
                .tableId(booking.getTable().getId())
                .tableNumber(booking.getTable().getTableNumber())
                .restaurantId(booking.getTable().getRestaurant().getId())
                .restaurantName(booking.getTable().getRestaurant().getName())
                .build();
    }
}
