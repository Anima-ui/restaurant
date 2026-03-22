package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Unified error response format")
public record ApiError(
        @Schema(example = "2026-03-22T14:40:00+03:00") OffsetDateTime timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Validation failed") String message,
        @Schema(example = "/api/v1/restaurants") String path,
        @ArraySchema(schema = @Schema(implementation = ApiFieldError.class))
        List<ApiFieldError> fieldErrors
) {
}
