package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.TransactionDemoResult;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.domain.model.RestaurantTable;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.repository.RestaurantTableRepository;
import com.restaurant.app.sevice.TransactionDemoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionDemoServiceImpl implements TransactionDemoService {

    private final RestaurantRepository restaurantRepository;

    private final RestaurantTableRepository tableRepository;

    public TransactionDemoServiceImpl(RestaurantRepository restaurantRepository,
                                      RestaurantTableRepository tableRepository) {
        this.restaurantRepository = restaurantRepository;
        this.tableRepository = tableRepository;
    }

    public TransactionDemoResult savePartiallyWithoutTransaction(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(1)
                .seats(2)
                .restaurant(savedRestaurant)
                .build();
        tableRepository.save(table);

        try {
            throw new IllegalStateException("Simulated failure after partial save");
        } catch (RuntimeException exception) {
            return TransactionDemoResult.builder()
                    .scenario("WITHOUT_TRANSACTION")
                    .restaurantsInDb(restaurantRepository.count())
                    .tablesInDb(tableRepository.count())
                    .note("Exception handled, previously saved records remain in DB.")
                    .build();
        }
    }

    @Transactional
    public TransactionDemoResult rollbackCompletelyWithTransaction(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(99)
                .seats(4)
                .restaurant(savedRestaurant)
                .build();
        tableRepository.save(table);

        throw new IllegalStateException("Simulated failure for rollback");
    }

    public TransactionDemoResult getCurrentState(String scenario, String note) {
        return TransactionDemoResult.builder()
                .scenario(scenario)
                .restaurantsInDb(restaurantRepository.count())
                .tablesInDb(tableRepository.count())
                .note(note)
                .build();
    }
}
