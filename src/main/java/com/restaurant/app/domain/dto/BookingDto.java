package com.restaurant.app.domain.dto;

import com.restaurant.app.domain.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    private LocalDateTime bookingTime;

    private BookingStatus status;

    private Long customerId;

    private String customerName;

    private Long tableId;

    private Integer tableNumber;

    private Long restaurantId;

    private String restaurantName;
}
