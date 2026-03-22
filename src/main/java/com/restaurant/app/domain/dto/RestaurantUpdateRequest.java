package com.restaurant.app.domain.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating restaurant basic information")
public class RestaurantUpdateRequest {
    @NotBlank
    @Schema(example = "Basilico Updated")
    private String name;

    @NotBlank
    @Schema(example = "Saint Petersburg")
    private String city;

    @NotBlank
    @Schema(example = "Mediterranean")
    private String cuisineType;
}
