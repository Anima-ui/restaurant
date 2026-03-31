package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight restaurant search result")
public class RestaurantSearchResultDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Basilico")
    private String name;

    @Schema(example = "Moscow")
    private String city;

    @Schema(example = "Italian")
    private String cuisineType;
}
