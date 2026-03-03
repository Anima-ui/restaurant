package com.restaurant.app.mapper;

import com.restaurant.app.domain.dto.DishDto;
import com.restaurant.app.domain.dto.DishRequest;
import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantTableDto;
import com.restaurant.app.domain.dto.RestaurantTableRequest;
import com.restaurant.app.domain.model.Dish;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.domain.model.Tag;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RestaurantMapper {

    public RestaurantDto toDto(Restaurant restaurant) {
        return RestaurantDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .city(restaurant.getCity())
                .cuisineType(restaurant.getCuisineType())
                .tables(restaurant.getTables().stream().map(this::toTableDto).toList())
                .dishes(restaurant.getDishes().stream().map(this::toDishDto).toList())
                .build();
    }

    public Restaurant toEntity(RestaurantCreateRequest dto) {
        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .cuisineType(dto.getCuisineType())
                .build();

        dto.getTables().stream()
                .map(this::toTableEntity)
                .forEach(restaurant::addTable);

        dto.getDishes().stream()
                .map(this::toDishEntity)
                .forEach(restaurant::addDish);

        return restaurant;
    }

    private RestaurantTableDto toTableDto(RestaurantTable table) {
        return RestaurantTableDto.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .seats(table.getSeats())
                .build();
    }

    private DishDto toDishDto(Dish dish) {
        return DishDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .tags(dish.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .build();
    }

    private RestaurantTable toTableEntity(RestaurantTableRequest tableRequest) {
        return RestaurantTable.builder()
                .tableNumber(tableRequest.getTableNumber())
                .seats(tableRequest.getSeats())
                .build();
    }

    private Dish toDishEntity(DishRequest dishRequest) {
        return Dish.builder()
                .name(dishRequest.getName())
                .price(dishRequest.getPrice())
                .tags(dishRequest.getTags().stream()
                        .map(tagName -> Tag.builder().name(tagName).build())
                        .collect(Collectors.toSet()))
                .build();
    }

}
