package com.restaurant.app.controller.transaction;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.TransactionDemoResult;
import com.restaurant.app.sevice.TransactionDemoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionDemoController {

    private final TransactionDemoService transactionDemoService;

    public TransactionDemoController(TransactionDemoService transactionDemoService) {
        this.transactionDemoService = transactionDemoService;
    }

    @PostMapping("/partial")
    public ResponseEntity<TransactionDemoResult> partialSave(@Valid @RequestBody RestaurantCreateRequest request) {
        return ResponseEntity.ok(transactionDemoService.savePartiallyWithoutTransaction(request));
    }

    @PostMapping("/rollback")
    public ResponseEntity<TransactionDemoResult> fullRollback(@Valid @RequestBody RestaurantCreateRequest request) {
        try {
            transactionDemoService.rollbackCompletelyWithTransaction(request);
            return ResponseEntity.ok(transactionDemoService.getCurrentState(
                    "WITH_TRANSACTION",
                    "No exception happened, data committed."
            ));
        } catch (RuntimeException exception) {
            return ResponseEntity.ok(transactionDemoService.getCurrentState(
                    "WITH_TRANSACTION",
                    "Exception thrown, transaction rolled back completely."
            ));
        }
    }
}
