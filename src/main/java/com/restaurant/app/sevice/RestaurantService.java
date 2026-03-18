package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantService {

    List<RestaurantDto> getAll();

    RestaurantDto getById(Long id);

    List<RestaurantDto> getByCity(String city);

    List<RestaurantDto> getDetailedByCity(String city);

    Page<RestaurantDto> searchByDishFiltersJpql(RestaurantSearchRequest request, Pageable pageable);

    Page<RestaurantDto> searchByDishFiltersNative(RestaurantSearchRequest request, Pageable pageable);

    RestaurantDto create(RestaurantCreateRequest dto);

    RestaurantDto update(Long id, RestaurantUpdateRequest dto);

    void delete(Long id);

    List<RestaurantDto> findAllRestaurantsDishesWithNPlusProblem();

    List<RestaurantDto> findAllRestaurantsDishesOptimized();

    List<RestaurantDto> findAllTablesWithBookingsNPlusProblem();

    List<RestaurantDto> findAllTablesWithBookingsOptimized();
}
