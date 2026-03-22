package com.restaurant.app.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for restaurant table creation")
public class RestaurantTableRequest {
    @NotNull
    @Min(1)
    @Schema(example = "1")
    private Integer tableNumber;

    @NotNull
    @Min(1)
    @Schema(example = "4")
    private Integer seats;
}
