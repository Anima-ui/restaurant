package com.restaurant.app.controller.transaction;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.TransactionDemoResult;
import com.restaurant.app.sevice.TransactionDemoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
                    "Exception thrown and transaction rolled back completely."
            ));
        }
    }

    @PostMapping("/cascade")
    public ResponseEntity<TransactionDemoResult> cascadeSave(@Valid @RequestBody RestaurantCreateRequest request) {
        return ResponseEntity.ok(transactionDemoService.saveWithCascade(request));
    }

    @PostMapping("/exception-no-tx")
    public ResponseEntity<TransactionDemoResult> exceptionWithoutTransaction(
            @Valid @RequestBody RestaurantCreateRequest request) {
        try {
            transactionDemoService.saveRestaurantAndThrowException(request);
        } catch (RuntimeException exception) {
            return ResponseEntity.ok(transactionDemoService.getCurrentState(
                    "EXCEPTION_NO_TX",
                    "Exception thrown after save, data remained in DB."
            ));
        }
        return ResponseEntity.ok(new TransactionDemoResult());
    }

    @PostMapping("/exception-with-tx")
    public ResponseEntity<TransactionDemoResult> exceptionWithTransaction(
            @Valid @RequestBody RestaurantCreateRequest request) {
        try {
            transactionDemoService.saveRestaurantAndThrowExceptionWithTransactional(request);
        } catch (RuntimeException exception) {
            return ResponseEntity.ok(transactionDemoService.getCurrentState(
                    "EXCEPTION_WITH_TX",
                    "Exception thrown and transaction rollback removed pending changes."
            ));
        }
        return ResponseEntity.ok(new TransactionDemoResult());
    }

    @GetMapping("/state")
    public ResponseEntity<TransactionDemoResult> getCurrentState() {
        return ResponseEntity.ok(transactionDemoService.getCurrentState(
                "CURRENT",
                "Current database state"
        ));
    }
}
