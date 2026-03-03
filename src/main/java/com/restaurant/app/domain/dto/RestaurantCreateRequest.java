package com.restaurant.app.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCreateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String cuisineType;

    @Valid
    @Builder.Default
    private List<RestaurantTableRequest> tables = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<DishRequest> dishes = new ArrayList<>();
}
