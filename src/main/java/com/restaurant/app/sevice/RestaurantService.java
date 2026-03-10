package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;

import java.util.List;

public interface RestaurantService {

    List<RestaurantDto> getAll();

    RestaurantDto getById(Long id);

    List<RestaurantDto> getByCity(String city);

    List<RestaurantDto> getDetailedByCity(String city);

    RestaurantDto create(RestaurantCreateRequest dto);

    RestaurantDto update(Long id, RestaurantUpdateRequest dto);

    void delete(Long id);

    List<RestaurantDto> findAllRestaurantsDishesWithNPlusProblem();

    List<RestaurantDto> findAllRestaurantsDishesOptimized();

    List<RestaurantDto> findAllTablesWithBookingsNPlusProblem();

    List<RestaurantDto> findAllTablesWithBookingsOptimized();
}
