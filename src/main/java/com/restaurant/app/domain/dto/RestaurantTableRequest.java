package com.restaurant.app.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableRequest {
    @NotNull
    @Min(1)
    private Integer tableNumber;

    @NotNull
    @Min(1)
    private Integer seats;
}
