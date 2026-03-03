package com.restaurant.app.controller.restaurant;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import com.restaurant.app.sevice.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController implements RestaurantAPI {

    private final RestaurantService restaurantServiceImpl;


    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantServiceImpl = restaurantService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<RestaurantDto>> getAll() {
        return ResponseEntity.ok(restaurantServiceImpl.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getById(@PathVariable Long id) {
        RestaurantDto dto = restaurantServiceImpl.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getByCity(@RequestParam String city) {
        List<RestaurantDto> list = restaurantServiceImpl.getByCity(city);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detailed")
    public ResponseEntity<List<RestaurantDto>> getDetailedByCity(@RequestParam String city) {
        return ResponseEntity.ok(restaurantServiceImpl.getDetailedByCity(city));
    }

    @PostMapping
    public ResponseEntity<RestaurantDto> create(@Valid @RequestBody RestaurantCreateRequest dto) {
        RestaurantDto created = restaurantServiceImpl.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> update(@PathVariable Long id,
                                                @Valid @RequestBody RestaurantUpdateRequest dto) {
        return ResponseEntity.ok(restaurantServiceImpl.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        restaurantServiceImpl.delete(id);
        return ResponseEntity.noContent().build();
    }
}
