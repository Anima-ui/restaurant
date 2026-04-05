package com.restaurant.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantDto {
    private Long id;

    private String name;

    private String city;

    private String cuisineType;

    @Builder.Default
    private List<RestaurantTableDto> tables = new ArrayList<>();

    @Builder.Default
    private List<DishDto> dishes = new ArrayList<>();

    @Builder.Default
    private List<String> amenities = new ArrayList<>();
}
