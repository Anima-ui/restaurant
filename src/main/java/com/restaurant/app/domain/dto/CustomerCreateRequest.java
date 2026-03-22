package com.restaurant.app.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a customer")
public class CustomerCreateRequest {
    @NotBlank
    @Schema(example = "Ivan Petrov")
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "phone must contain 10 to 15 digits and optional leading '+'")
    @Schema(example = "+79990001122")
    private String phone;
}
