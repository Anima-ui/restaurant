package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Search filters for restaurants by nested dish data")
public record RestaurantSearchRequest(
        @Schema(example = "Moscow")
        @Size(max = 100)
        String city,
        @Schema(example = "Italian")
        @Size(max = 100)
        String cuisineType,
        @Schema(example = "pasta")
        @Size(max = 100)
        String dishName,
        @Schema(example = "10.00")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal minDishPrice,
        @Schema(example = "30.00")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal maxDishPrice
) {

    public RestaurantSearchRequest {
        city = normalize(city);
        cuisineType = normalize(cuisineType);
        dishName = normalize(dishName);
    }

    public String sortDescription(Sort sort) {
        return sort.stream()
                .map(order -> order.getProperty() + ":" + order.getDirection().name())
                .reduce((left, right) -> left + "," + right)
                .orElse("UNSORTED");
    }

    private static String normalize(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
