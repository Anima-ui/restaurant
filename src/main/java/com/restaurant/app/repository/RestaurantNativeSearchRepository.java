package com.restaurant.app.repository;

import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface RestaurantNativeSearchRepository {

    Page<RestaurantSearchResultDto> searchByDishFiltersNative(String city,
                                                              String cuisineType,
                                                              String dishNamePattern,
                                                              BigDecimal minDishPrice,
                                                              BigDecimal maxDishPrice,
                                                              Pageable pageable);
}
