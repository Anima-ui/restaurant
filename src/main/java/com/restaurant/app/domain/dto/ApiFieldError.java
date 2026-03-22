package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error for a specific field")
public record ApiFieldError(
        @Schema(example = "name") String field,
        @Schema(example = "must not be blank") String message
) {
}
