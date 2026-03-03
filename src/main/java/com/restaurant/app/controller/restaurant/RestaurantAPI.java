package com.restaurant.app.controller.restaurant;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RestaurantAPI {

    ResponseEntity<List<RestaurantDto>> getAll();

    ResponseEntity<RestaurantDto> getById(@PathVariable Long id);

    ResponseEntity<List<RestaurantDto>> getByCity(@RequestParam String city);

    ResponseEntity<List<RestaurantDto>> getDetailedByCity(@RequestParam String city);

    ResponseEntity<RestaurantDto> create(@Valid @RequestBody RestaurantCreateRequest dto);

    ResponseEntity<RestaurantDto> update(@PathVariable Long id, @Valid @RequestBody RestaurantUpdateRequest dto);

    ResponseEntity<Void> delete(@PathVariable Long id);
}
