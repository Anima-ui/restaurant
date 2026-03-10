package com.restaurant.app.domain.dto;

import com.restaurant.app.domain.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequest {
    @NotNull
    private LocalDateTime bookingTime;

    @NotNull
    private BookingStatus status;

    @NotNull
    private Long customerId;

    @NotNull
    private Long tableId;
}
