package com.restaurant.app.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request for creating a restaurant with tables and dishes")
public class RestaurantCreateRequest {
    @NotBlank
    @Schema(example = "Basilico")
    private String name;

    @NotBlank
    @Schema(example = "Moscow")
    private String city;

    @NotBlank
    @Schema(example = "Italian")
    private String cuisineType;

    @Valid
    @NotEmpty
    @Builder.Default
    @ArraySchema(schema = @Schema(implementation = RestaurantTableRequest.class))
    private List<RestaurantTableRequest> tables = new ArrayList<>();

    @Valid
    @NotEmpty
    @Builder.Default
    @ArraySchema(schema = @Schema(implementation = DishRequest.class))
    private List<DishRequest> dishes = new ArrayList<>();

    @Builder.Default
    @ArraySchema(schema = @Schema(implementation = String.class))
    private List<String> amenities = new ArrayList<>();
}
