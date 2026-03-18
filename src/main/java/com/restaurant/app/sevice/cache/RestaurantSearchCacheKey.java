package com.restaurant.app.sevice.cache;

import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.sevice.RestaurantSearchMode;

import java.math.BigDecimal;

public record RestaurantSearchCacheKey(
        RestaurantSearchMode mode,
        String city,
        String cuisineType,
        String dishName,
        BigDecimal minDishPrice,
        BigDecimal maxDishPrice,
        int pageNumber,
        int pageSize,
        String sort
) {

    public static RestaurantSearchCacheKey of(RestaurantSearchMode mode,
                                              RestaurantSearchRequest request,
                                              int pageNumber,
                                              int pageSize,
                                              String sort) {
        return new RestaurantSearchCacheKey(
                mode,
                request.city(),
                request.cuisineType(),
                request.dishName(),
                request.minDishPrice(),
                request.maxDishPrice(),
                pageNumber,
                pageSize,
                sort
        );
    }
}
