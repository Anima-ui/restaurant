package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of bulk customer operation")
public class CustomerBulkResult {

    @Schema(example = "CUSTOMER_BULK")
    private String scenario;

    @Schema(example = "3")
    private int requestedCount;

    @Schema(example = "2")
    private int savedCount;

    @Schema(example = "5")
    private long customersInDb;

    private List<CustomerDto> savedCustomers;

    @Schema(example = "Bulk operation completed successfully")
    private String note;
}
