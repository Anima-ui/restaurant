package com.restaurant.app.domain.dto;

import com.restaurant.app.domain.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdateRequest {
    @NotNull
    private BookingStatus status;
}
