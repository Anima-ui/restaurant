package com.restaurant.app.domain.dto;

import com.restaurant.app.domain.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a booking")
public class BookingCreateRequest {
    @NotNull
    @Future(message = "bookingTime must be in the future")
    @Schema(example = "2026-04-01T19:00:00")
    private LocalDateTime bookingTime;

    @NotNull
    @Schema(example = "CREATED")
    private BookingStatus status;

    @NotNull
    @Positive
    @Schema(example = "1")
    private Long customerId;

    @NotNull
    @Positive
    @Schema(example = "1")
    private Long tableId;
}
