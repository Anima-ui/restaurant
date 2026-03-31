package com.restaurant.app.controller.restaurant;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RestaurantAPI {

    ResponseEntity<List<RestaurantDto>> getAll();

    ResponseEntity<RestaurantDto> getById(@PathVariable @Positive Long id);

    ResponseEntity<List<RestaurantDto>> getByCity(@RequestParam @NotBlank String city);

    ResponseEntity<List<RestaurantDto>> getDetailedByCity(@RequestParam @NotBlank String city);

    ResponseEntity<Page<RestaurantSearchResultDto>> searchByDishFiltersJpql(@Valid RestaurantSearchRequest request,
                                                                            Pageable pageable);

    ResponseEntity<Page<RestaurantSearchResultDto>> searchByDishFiltersNative(@Valid RestaurantSearchRequest request,
                                                                              Pageable pageable);

    ResponseEntity<RestaurantDto> create(@Valid @RequestBody RestaurantCreateRequest dto);

    ResponseEntity<RestaurantDto> update(@PathVariable @Positive Long id,
                                         @Valid @RequestBody RestaurantUpdateRequest dto);

    ResponseEntity<Void> delete(@PathVariable @Positive Long id);
}
