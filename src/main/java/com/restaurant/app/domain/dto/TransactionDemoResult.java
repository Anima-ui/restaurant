package com.restaurant.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDemoResult {
    private String scenario;

    private long restaurantsInDb;

    private long tablesInDb;

    private String note;
}
