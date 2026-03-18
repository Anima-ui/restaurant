package com.restaurant.app.domain.dto;

import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Objects;

public record RestaurantSearchRequest(
        String city,
        String cuisineType,
        String dishName,
        BigDecimal minDishPrice,
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
