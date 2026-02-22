package com.restaurant.app.controller.RestaurantEndpoint;

import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.sevice.RestaurantService;
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

    @PostMapping
    public ResponseEntity<RestaurantDto> create(@RequestBody RestaurantDto dto) {
        RestaurantDto created = restaurantServiceImpl.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
