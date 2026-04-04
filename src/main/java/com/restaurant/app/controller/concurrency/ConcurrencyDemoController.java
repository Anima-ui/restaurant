package com.restaurant.app.controller.concurrency;

import com.restaurant.app.domain.dto.RaceConditionDemoResult;
import com.restaurant.app.sevice.ConcurrencyDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/concurrency")
@Validated
@Tag(name = "Concurrency", description = "Async and race condition demonstration endpoints")
public class ConcurrencyDemoController {

    private final ConcurrencyDemoService concurrencyDemoService;

    public ConcurrencyDemoController(ConcurrencyDemoService concurrencyDemoService) {
        this.concurrencyDemoService = concurrencyDemoService;
    }

    @GetMapping("/race-condition/unsafe")
    @Operation(summary = "Run unsafe counter demo with 50+ threads")
    public ResponseEntity<RaceConditionDemoResult> runUnsafeCounterDemo(
            @RequestParam(defaultValue = "64") @Min(value = 50, message = "threads must be at least 50") int threads,
            @RequestParam(defaultValue = "1000") @Min(value = 1, message = "incrementsPerThread must be positive")
            int incrementsPerThread) {
        return ResponseEntity.ok(concurrencyDemoService.runUnsafeCounterDemo(threads, incrementsPerThread));
    }

    @GetMapping("/race-condition/atomic")
    @Operation(summary = "Run AtomicInteger based counter demo with 50+ threads")
    public ResponseEntity<RaceConditionDemoResult> runAtomicCounterDemo(
            @RequestParam(defaultValue = "64") @Min(value = 50, message = "threads must be at least 50") int threads,
            @RequestParam(defaultValue = "1000") @Min(value = 1, message = "incrementsPerThread must be positive")
            int incrementsPerThread) {
        return ResponseEntity.ok(concurrencyDemoService.runAtomicCounterDemo(threads, incrementsPerThread));
    }
}
