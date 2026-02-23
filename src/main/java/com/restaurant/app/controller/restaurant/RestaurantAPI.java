package com.restaurant.app.controller.restaurant;

import com.restaurant.app.domain.dto.RestaurantDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RestaurantAPI {

    ResponseEntity<RestaurantDto> getById(@PathVariable Long id);

    ResponseEntity<List<RestaurantDto>> getByCity(@RequestParam String city);

    ResponseEntity<RestaurantDto> create(@RequestBody RestaurantDto dto);
}
