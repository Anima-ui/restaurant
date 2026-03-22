package com.restaurant.app.domain.dto;

import com.restaurant.app.domain.model.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating booking status")
public class BookingStatusUpdateRequest {
    @NotNull
    @Schema(example = "CONFIRMED")
    private BookingStatus status;
}
