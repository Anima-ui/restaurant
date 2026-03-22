package com.restaurant.app.controller.booking;

import com.restaurant.app.domain.dto.BookingCreateRequest;
import com.restaurant.app.domain.dto.BookingDto;
import com.restaurant.app.domain.dto.BookingStatusUpdateRequest;
import com.restaurant.app.domain.model.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.restaurant.app.sevice.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@Validated
@Tag(name = "Bookings", description = "Booking operations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create booking")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody BookingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all bookings")
    public ResponseEntity<List<BookingDto>> getAll() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by id")
    public ResponseEntity<BookingDto> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update booking status")
    public ResponseEntity<BookingDto> updateStatus(@PathVariable @Positive Long id,
                                                   @Valid @RequestBody BookingStatusUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get available booking statuses")
    public ResponseEntity<List<BookingStatus>> getStatuses() {
        return ResponseEntity.ok(bookingService.getAvailableStatuses());
    }
}
