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
@Schema(description = "Response returned after async task submission")
public class AsyncTaskStartResponse {

    @Schema(example = "1")
    private Long taskId;

    @Schema(example = "PENDING")
    private AsyncTaskStatus status;

    @Schema(example = "Async customer bulk task has been accepted")
    private String message;
}
