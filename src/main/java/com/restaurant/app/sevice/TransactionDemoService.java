package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.TransactionDemoResult;

public interface TransactionDemoService {
    TransactionDemoResult savePartiallyWithoutTransaction(RestaurantCreateRequest request);

    TransactionDemoResult rollbackCompletelyWithTransaction(RestaurantCreateRequest request);

    TransactionDemoResult getCurrentState(String scenario, String note);
}
