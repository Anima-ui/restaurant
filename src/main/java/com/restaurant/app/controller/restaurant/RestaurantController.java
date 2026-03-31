package com.restaurant.app.controller.restaurant;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.restaurant.app.sevice.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@Validated
@Tag(name = "Restaurants", description = "Restaurant CRUD and search operations")
public class RestaurantController implements RestaurantAPI {

    private final RestaurantService restaurantServiceImpl;


    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantServiceImpl = restaurantService;
    }

    @GetMapping("/all")
    @Operation(summary = "Get all restaurants")
    public ResponseEntity<List<RestaurantDto>> getAll() {
        return ResponseEntity.ok(restaurantServiceImpl.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by id")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    public ResponseEntity<RestaurantDto> getById(@PathVariable @Positive Long id) {
        RestaurantDto dto = restaurantServiceImpl.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "Get restaurants by city")
    public ResponseEntity<List<RestaurantDto>> getByCity(@RequestParam @NotBlank String city) {
        List<RestaurantDto> list = restaurantServiceImpl.getByCity(city);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detailed")
    @Operation(summary = "Get detailed restaurants by city")
    public ResponseEntity<List<RestaurantDto>> getDetailedByCity(@RequestParam @NotBlank String city) {
        return ResponseEntity.ok(restaurantServiceImpl.getDetailedByCity(city));
    }

    @GetMapping("/search/jpql")
    @Operation(summary = "Search restaurants with JPQL and Pageable")
    public ResponseEntity<Page<RestaurantSearchResultDto>> searchByDishFiltersJpql(
            @Valid RestaurantSearchRequest request,
            Pageable pageable) {
        return ResponseEntity.ok(restaurantServiceImpl.searchByDishFiltersJpql(request, pageable));
    }

    @GetMapping("/search/native")
    @Operation(summary = "Search restaurants with native SQL and Pageable")
    public ResponseEntity<Page<RestaurantSearchResultDto>> searchByDishFiltersNative(
            @Valid RestaurantSearchRequest request,
            Pageable pageable) {
        return ResponseEntity.ok(restaurantServiceImpl.searchByDishFiltersNative(request, pageable));
    }

    @PostMapping
    @Operation(summary = "Create restaurant")
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = com.restaurant.app.domain.dto.ApiError.class)))
    public ResponseEntity<RestaurantDto> create(@Valid @RequestBody RestaurantCreateRequest dto) {
        RestaurantDto created = restaurantServiceImpl.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant")
    public ResponseEntity<RestaurantDto> update(@PathVariable @Positive Long id,
                                                @Valid @RequestBody RestaurantUpdateRequest dto) {
        return ResponseEntity.ok(restaurantServiceImpl.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete restaurant")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        restaurantServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }
}
