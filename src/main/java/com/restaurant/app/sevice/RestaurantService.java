package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.RestaurantDto;

import java.util.List;

public interface RestaurantService {

    RestaurantDto getById(Long id);

    List<RestaurantDto> getByCity(String city);

    RestaurantDto create(RestaurantDto dto);
}
