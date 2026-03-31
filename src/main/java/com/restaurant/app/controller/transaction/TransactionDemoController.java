package com.restaurant.app.controller.transaction;

import com.restaurant.app.domain.dto.CustomerBulkCreateRequest;
import com.restaurant.app.domain.dto.CustomerBulkResult;
import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.TransactionDemoResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.restaurant.app.sevice.TransactionDemoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
@Tag(name = "Transactions", description = "Transaction demonstration endpoints")
public class TransactionDemoController {

    private final TransactionDemoService transactionDemoService;

    public TransactionDemoController(TransactionDemoService transactionDemoService) {
        this.transactionDemoService = transactionDemoService;
    }

    @PostMapping("/partial")
    @Operation(summary = "Save partially without transaction")
    public ResponseEntity<TransactionDemoResult> partialSave(@Valid @RequestBody RestaurantCreateRequest request) {
        return ResponseEntity.ok(transactionDemoService.savePartiallyWithoutTransaction(request));
    }

    @PostMapping("/rollback")
    @Operation(summary = "Trigger rollback within transaction")
    public ResponseEntity<TransactionDemoResult> fullRollback(@Valid @RequestBody RestaurantCreateRequest request) {
        transactionDemoService.rollbackCompletelyWithTransaction(request);
        return ResponseEntity.ok(transactionDemoService.getCurrentState(
                "WITH_TRANSACTION",
                "No exception happened, data committed."
        ));
    }

    @PostMapping("/cascade")
    @Operation(summary = "Save restaurant and tables with cascade")
    public ResponseEntity<TransactionDemoResult> cascadeSave(@Valid @RequestBody RestaurantCreateRequest request) {
        return ResponseEntity.ok(transactionDemoService.saveWithCascade(request));
    }

    @PostMapping("/exception-no-tx")
    @Operation(summary = "Throw exception after save without transaction")
    public ResponseEntity<TransactionDemoResult> exceptionWithoutTransaction(
            @Valid @RequestBody RestaurantCreateRequest request) {
        transactionDemoService.saveRestaurantAndThrowException(request);
        return ResponseEntity.ok(new TransactionDemoResult());
    }

    @PostMapping("/exception-with-tx")
    @Operation(summary = "Throw exception after save with transaction")
    public ResponseEntity<TransactionDemoResult> exceptionWithTransaction(
            @Valid @RequestBody RestaurantCreateRequest request) {
        transactionDemoService.saveRestaurantAndThrowExceptionWithTransactional(request);
        return ResponseEntity.ok(new TransactionDemoResult());
    }

    @PostMapping("/customers/bulk-no-tx")
    @Operation(summary = "Bulk create customers without transaction")
    public ResponseEntity<CustomerBulkResult> bulkCustomersWithoutTransaction(
            @Valid @RequestBody CustomerBulkCreateRequest request) {
        return ResponseEntity.ok(transactionDemoService.bulkCreateCustomersWithoutTransaction(request));
    }

    @PostMapping("/customers/bulk-with-tx")
    @Operation(summary = "Bulk create customers with transaction")
    public ResponseEntity<CustomerBulkResult> bulkCustomersWithTransaction(
            @Valid @RequestBody CustomerBulkCreateRequest request) {
        return ResponseEntity.ok(transactionDemoService.bulkCreateCustomersWithTransaction(request));
    }

    @GetMapping("/state")
    @Operation(summary = "Get current transaction demo state")
    public ResponseEntity<TransactionDemoResult> getCurrentState() {
        return ResponseEntity.ok(transactionDemoService.getCurrentState(
                "CURRENT",
                "Current database state"
        ));
    }
}
