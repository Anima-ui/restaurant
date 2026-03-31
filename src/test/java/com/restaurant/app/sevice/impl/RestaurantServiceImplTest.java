package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import com.restaurant.app.domain.model.Booking;
import com.restaurant.app.domain.model.Dish;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void getAllReturnsMappedRestaurants() {
        Restaurant restaurant = restaurant(1L, "Roma");
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findAll()).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.getAll()).containsExactly(dto);
    }

    @Test
    void getByIdReturnsMappedRestaurant() {
        Restaurant restaurant = restaurant(1L, "Roma");
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.getById(1L)).isEqualTo(dto);
    }

    @Test
    void getByIdThrowsWhenRestaurantIsMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant with id=1");
    }

    @Test
    void getByCityReturnsMappedRestaurants() {
        Restaurant restaurant = restaurant(1L, "Roma");
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findByCity("Moscow")).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.getByCity("Moscow")).containsExactly(dto);
    }

    @Test
    void getDetailedByCityReturnsMappedRestaurants() {
        Restaurant restaurant = restaurant(1L, "Roma");
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findDetailedByCity("Moscow")).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.getDetailedByCity("Moscow")).containsExactly(dto);
    }

    @Test
    void searchByDishFiltersJpqlUsesCacheOnRepeatedRequest() {
        RestaurantSearchRequest request = new RestaurantSearchRequest(
                " Moscow ",
                "Italian",
                "Pasta",
                BigDecimal.TEN,
                new BigDecimal("50")
        );
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        PageImpl<RestaurantSearchResultDto> page = new PageImpl<>(List.of(searchDto(1L, "Roma")), pageable, 1);

        when(restaurantSearchCache.get(any()))
                .thenReturn(Optional.empty(), Optional.of(page));
        when(repository.searchByDishFiltersJpql(
                "moscow",
                "italian",
                "%pasta%",
                request.minDishPrice(),
                request.maxDishPrice(),
                pageable
        )).thenReturn(page);

        assertThat(restaurantService.searchByDishFiltersJpql(request, pageable)).isEqualTo(page);
        assertThat(restaurantService.searchByDishFiltersJpql(request, pageable)).isEqualTo(page);

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
    void searchByDishFiltersNativeNormalizesNullsAndCachesResult() {
        RestaurantSearchRequest request = new RestaurantSearchRequest(
                "   ",
                null,
                null,
                null,
                null
        );
        PageRequest pageable = PageRequest.of(0, 2);
        PageImpl<RestaurantSearchResultDto> page = new PageImpl<>(List.of(searchDto(1L, "Roma")), pageable, 1);

        when(restaurantSearchCache.get(any())).thenReturn(Optional.empty());
        when(repository.searchByDishFiltersNative(null, null, null, null, null, pageable)).thenReturn(page);

        assertThat(restaurantService.searchByDishFiltersNative(request, pageable)).isEqualTo(page);

        verify(repository).searchByDishFiltersNative(null, null, null, null, null, pageable);
        verify(restaurantSearchCache).put(any(), any());
    }

    @Test
    void createClearsSearchCacheAfterMutation() {
        RestaurantCreateRequest request = RestaurantCreateRequest.builder()
                .name("Roma")
                .city("Moscow")
                .cuisineType("Italian")
                .build();
        Restaurant restaurant = restaurant(1L, "Roma");
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(mapper.toEntity(request)).thenReturn(restaurant);
        when(repository.save(restaurant)).thenReturn(restaurant);
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.create(request)).isEqualTo(dto);

        verify(restaurantSearchCache).clear();
    }

    @Test
    void updateChangesRestaurantAndClearsCache() {
        RestaurantUpdateRequest request = RestaurantUpdateRequest.builder()
                .name("Basilico")
                .city("Saint Petersburg")
                .cuisineType("Italian")
                .build();
        Restaurant restaurant = restaurant(1L, "Roma");
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Basilico").build();

        when(repository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(repository.save(restaurant)).thenReturn(restaurant);
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.update(1L, request)).isEqualTo(dto);
        assertThat(restaurant.getName()).isEqualTo("Basilico");
        assertThat(restaurant.getCity()).isEqualTo("Saint Petersburg");
        assertThat(restaurant.getCuisineType()).isEqualTo("Italian");
        verify(restaurantSearchCache).clear();
    }

    @Test
    void updateThrowsWhenRestaurantIsMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.update(1L, RestaurantUpdateRequest.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant with id=1");
    }

    @Test
    void deleteClearsSearchCacheAfterMutation() {
        Restaurant restaurant = restaurant(1L, "Roma");

        when(repository.findById(1L)).thenReturn(Optional.of(restaurant));

        restaurantService.delete(1L);

        verify(repository).delete(restaurant);
        verify(restaurantSearchCache).clear();
        verify(repository, never()).save(any());
    }

    @Test
    void deleteThrowsWhenRestaurantIsMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant with id=1");
    }

    @Test
    void findAllRestaurantsDishesWithNPlusProblemLoadsAndMapsRestaurants() {
        Restaurant restaurant = restaurantWithDishAndBooking();
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findAllWithoutFetch()).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.findAllRestaurantsDishesWithNPlusProblem()).containsExactly(dto);
    }

    @Test
    void findAllRestaurantsDishesOptimizedLoadsAndMapsRestaurants() {
        Restaurant restaurant = restaurantWithDishAndBooking();
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findAllWithDishesJoinFetch()).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.findAllRestaurantsDishesOptimized()).containsExactly(dto);
    }

    @Test
    void findAllTablesWithBookingsNPlusProblemLoadsAndMapsRestaurants() {
        Restaurant restaurant = restaurantWithDishAndBooking();
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findAllWithoutFetch()).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.findAllTablesWithBookingsNPlusProblem()).containsExactly(dto);
    }

    @Test
    void findAllTablesWithBookingsOptimizedLoadsAndMapsRestaurants() {
        Restaurant restaurant = restaurantWithDishAndBooking();
        RestaurantDto dto = RestaurantDto.builder().id(1L).name("Roma").build();

        when(repository.findAllWithTablesAndBookings()).thenReturn(List.of(restaurant));
        when(mapper.toDto(restaurant)).thenReturn(dto);

        assertThat(restaurantService.findAllTablesWithBookingsOptimized()).containsExactly(dto);
    }

    private RestaurantSearchResultDto searchDto(Long id, String name) {
        return RestaurantSearchResultDto.builder()
                .id(id)
                .name(name)
                .city("Moscow")
                .cuisineType("Italian")
                .build();
    }

    private Restaurant restaurant(Long id, String name) {
        return Restaurant.builder()
                .id(id)
                .name(name)
                .city("Moscow")
                .cuisineType("Italian")
                .build();
    }

    private Restaurant restaurantWithDishAndBooking() {
        Restaurant restaurant = restaurant(1L, "Roma");
        restaurant.addDish(Dish.builder()
                .id(1L)
                .name("Pasta")
                .price(new BigDecimal("25.00"))
                .build());

        RestaurantTable table = RestaurantTable.builder()
                .id(2L)
                .tableNumber(5)
                .seats(4)
                .restaurant(restaurant)
                .build();
        table.getBookings().add(Booking.builder().id(3L).table(table).build());
        restaurant.getTables().add(table);
        return restaurant;
    }
}
