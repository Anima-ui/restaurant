package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.domain.model.Dish;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.mapper.RestaurantMapper;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.sevice.cache.RestaurantSearchCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository repository;

    @Mock
    private RestaurantMapper mapper;

    @Mock
    private RestaurantSearchCache restaurantSearchCache;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @Test
    void searchByDishFiltersJpqlUsesCacheOnRepeatedRequest() {
        RestaurantSearchRequest request = new RestaurantSearchRequest(
                "Moscow",
                "Italian",
                "Pasta",
                BigDecimal.TEN,
                new BigDecimal("50")
        );
        PageRequest pageable = PageRequest.of(0, 5);
        Restaurant restaurant = restaurantWithDish();

        when(restaurantSearchCache.get(any()))
                .thenReturn(Optional.empty(), Optional.of(new PageImpl<>(List.of(), pageable, 0)));
        when(repository.searchByDishFiltersJpql(
                "moscow",
                "italian",
                "%pasta%",
                request.minDishPrice(),
                request.maxDishPrice(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(restaurant), pageable, 1));
        when(mapper.toDto(restaurant)).thenReturn(null);

        restaurantService.searchByDishFiltersJpql(request, pageable);
        restaurantService.searchByDishFiltersJpql(request, pageable);

        verify(repository, times(1)).searchByDishFiltersJpql(
                "moscow",
                "italian",
                "%pasta%",
                request.minDishPrice(),
                request.maxDishPrice(),
                pageable
        );
        verify(restaurantSearchCache, times(1)).put(any(), any());
    }

    @Test
    void createClearsSearchCacheAfterMutation() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();
        Restaurant restaurant = Restaurant.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();

        when(mapper.toEntity(request)).thenReturn(restaurant);
        when(repository.save(restaurant)).thenReturn(restaurant);
        when(mapper.toDto(restaurant)).thenReturn(null);

        restaurantService.create(request);

        verify(restaurantSearchCache).clear();
    }

    @Test
    void deleteClearsSearchCacheAfterMutation() {
        Restaurant restaurant = restaurantWithDish();

        when(repository.findById(1L)).thenReturn(Optional.of(restaurant));

        restaurantService.delete(1L);

        verify(repository).delete(restaurant);
        verify(restaurantSearchCache).clear();
        verify(repository, never()).save(any());
    }

    private Restaurant restaurantWithDish() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();
        restaurant.addDish(Dish.builder()
                .id(1L)
                .name("Pasta")
                .price(new BigDecimal("25.00"))
                .build());
        return restaurant;
    }
}
