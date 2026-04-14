package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.BookingCreateRequest;
import com.restaurant.app.domain.dto.BookingDto;
import com.restaurant.app.domain.model.Booking;
import com.restaurant.app.domain.model.BookingStatus;
import com.restaurant.app.domain.model.Customer;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.exception.ConflictOperationException;
import com.restaurant.app.exception.ResourceNotFoundException;
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

    private static final String NOT_FOUND_SUFFIX = " was not found";

    private static final String CUSTOMER_NOT_FOUND_PREFIX = "Customer with id=";

    private static final String RESTAURANT_TABLE_NOT_FOUND_PREFIX = "Restaurant table with id=";

    private static final String BOOKING_NOT_FOUND_PREFIX = "Booking with id=";

    private static final String TABLE_CAPACITY_EXCEEDED_MESSAGE =
            "Not enough free seats for the selected table and time";

    private static final String REQUESTED_SEATS_EXCEED_TABLE_CAPACITY_MESSAGE =
            "Requested seats exceed the table capacity";

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
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        CUSTOMER_NOT_FOUND_PREFIX + request.getCustomerId() + NOT_FOUND_SUFFIX));
        RestaurantTable table = restaurantTableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        RESTAURANT_TABLE_NOT_FOUND_PREFIX + request.getTableId() + NOT_FOUND_SUFFIX));

        if (request.getGuestCount() > table.getSeats()) {
            throw new ConflictOperationException(REQUESTED_SEATS_EXCEED_TABLE_CAPACITY_MESSAGE);
        }

        long reservedSeatsForTable = bookingRepository.sumGuestCountByTableIdAndBookingTimeAndStatusIn(
                request.getTableId(),
                request.getBookingTime(),
                List.of(BookingStatus.CREATED, BookingStatus.CONFIRMED)
        );
        if (reservedSeatsForTable + request.getGuestCount() > table.getSeats()) {
            throw new ConflictOperationException(TABLE_CAPACITY_EXCEEDED_MESSAGE);
        }

        Booking booking = Booking.builder()
                .bookingTime(request.getBookingTime())
                .status(request.getStatus())
                .guestCount(request.getGuestCount())
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
        return toDto(bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX)));
    }

    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(BOOKING_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX));
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
                .guestCount(booking.getGuestCount())
                .restaurantId(booking.getTable().getRestaurant().getId())
                .restaurantName(booking.getTable().getRestaurant().getName())
                .build();
    }
}
