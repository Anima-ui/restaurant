package com.restaurant.app.sevice.cache;

import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.sevice.RestaurantSearchMode;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        RestaurantSearchCacheKey that = (RestaurantSearchCacheKey) object;
        return pageNumber == that.pageNumber
                && pageSize == that.pageSize
                && mode == that.mode
                && Objects.equals(city, that.city)
                && Objects.equals(cuisineType, that.cuisineType)
                && Objects.equals(dishName, that.dishName)
                && Objects.equals(minDishPrice, that.minDishPrice)
                && Objects.equals(maxDishPrice, that.maxDishPrice)
                && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mode,
                city,
                cuisineType,
                dishName,
                minDishPrice,
                maxDishPrice,
                pageNumber,
                pageSize,
                sort
        );
    }
}
