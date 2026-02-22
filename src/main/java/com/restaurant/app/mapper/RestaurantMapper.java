package com.restaurant.app.mapper;

import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.model.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {

    public RestaurantDto toDto(Restaurant restaurant) {
        return RestaurantDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .city(restaurant.getCity())
                .cuisineType(restaurant.getCuisineType())
                .build();
    }

    public Restaurant toEntity(RestaurantDto dto) {
        return Restaurant.builder()
                .id(dto.getId())
                .name(dto.getName())
                .city(dto.getCity())
                .cuisineType(dto.getCuisineType())
                .build();
    }

}
