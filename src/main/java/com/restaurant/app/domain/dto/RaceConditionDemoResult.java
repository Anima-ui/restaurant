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
@Schema(description = "Result of race condition demonstration")
public class RaceConditionDemoResult {

    @Schema(example = "UNSAFE_COUNTER")
    private String scenario;

    @Schema(example = "64")
    private int threads;

    @Schema(example = "1000")
    private int incrementsPerThread;

    @Schema(example = "64000")
    private int expectedValue;

    @Schema(example = "61234")
    private int actualValue;

    @Schema(example = "2766")
    private int lostUpdates;

    @Schema(example = "Unsafe counter demonstrates race condition")
    private String note;
}
