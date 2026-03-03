package com.restaurant.app.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUpdateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String cuisineType;
}
