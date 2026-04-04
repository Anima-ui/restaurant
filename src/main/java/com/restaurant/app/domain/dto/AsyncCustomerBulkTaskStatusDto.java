package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current async bulk customer task status")
public class AsyncCustomerBulkTaskStatusDto {

    @Schema(example = "1")
    private Long taskId;

    @Schema(example = "RUNNING")
    private AsyncTaskStatus status;

    @Schema(example = "2")
    private int requestedCount;

    @Schema(example = "2")
    private int savedCount;

    @Schema(example = "Bulk operation completed successfully")
    private String note;

    @Schema(example = "Customers already exist for phones: [+79990001122]")
    private String errorMessage;

    private OffsetDateTime createdAt;

    private OffsetDateTime startedAt;

    private OffsetDateTime completedAt;
}
