package com.restaurant.app.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dish creation request")
public class DishRequest {
    @NotBlank
    @Schema(example = "Pasta Carbonara")
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(example = "14.00")
    private BigDecimal price;
}
