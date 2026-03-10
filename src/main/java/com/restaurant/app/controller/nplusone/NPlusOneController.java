package com.restaurant.app.controller.nplusone;

import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.sevice.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nplusone")
public class NPlusOneController {

    private final RestaurantService restaurantService;

    public NPlusOneController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/problem")
    public ResponseEntity<List<RestaurantDto>> nPlusProblem() {
        return ResponseEntity.ok(restaurantService.findAllRestaurantsDishesWithNPlusProblem());
    }

    @GetMapping("/optimized")
    public ResponseEntity<List<RestaurantDto>> nPlusOptimized() {
        return ResponseEntity.ok(restaurantService.findAllRestaurantsDishesOptimized());
    }

    @GetMapping("/nested-problem")
    public ResponseEntity<List<RestaurantDto>> nestedNPlusProblem() {
        return ResponseEntity.ok(restaurantService.findAllTablesWithBookingsNPlusProblem());
    }

    @GetMapping("/nested-optimized")
    public ResponseEntity<List<RestaurantDto>> nestedNPlusOptimized() {
        return ResponseEntity.ok(restaurantService.findAllTablesWithBookingsOptimized());
    }
}
