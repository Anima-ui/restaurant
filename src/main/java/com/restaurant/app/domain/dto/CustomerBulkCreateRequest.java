package com.restaurant.app.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk customer create request")
public class CustomerBulkCreateRequest {

    @Valid
    @NotEmpty
    @Schema(description = "Customers to create in one operation")
    private List<CustomerCreateRequest> customers;
}
