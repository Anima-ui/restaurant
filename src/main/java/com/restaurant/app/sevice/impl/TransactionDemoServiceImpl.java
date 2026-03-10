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
                .name(request.getName() + " (WITHOUT_TRANSACTION)")
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
            throw new IllegalStateException("Simulated failure after save without transaction");
        } catch (RuntimeException exception) {
            return TransactionDemoResult.builder()
                    .scenario("WITHOUT_TRANSACTION")
                    .restaurantsInDb(restaurantRepository.count())
                    .tablesInDb(tableRepository.count())
                    .note("Exception happened, data remained committed without transaction.")
                    .build();
        }
    }

    @Transactional
    public TransactionDemoResult rollbackCompletelyWithTransaction(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (WITH_TRANSACTION)")
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

        throw new IllegalStateException("Simulated failure to trigger rollback");
    }

    @Transactional
    public TransactionDemoResult saveWithCascade(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (WITH_CASCADE)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        RestaurantTable table1 = RestaurantTable.builder()
                .tableNumber(101)
                .seats(2)
                .restaurant(restaurant)
                .build();
        RestaurantTable table2 = RestaurantTable.builder()
                .tableNumber(102)
                .seats(4)
                .restaurant(restaurant)
                .build();

        restaurant.getTables().add(table1);
        restaurant.getTables().add(table2);
        restaurantRepository.save(restaurant);

        return TransactionDemoResult.builder()
                .scenario("CASCADE_SAVE")
                .restaurantsInDb(restaurantRepository.count())
                .tablesInDb(tableRepository.count())
                .note("Restaurant and tables were saved via cascade.")
                .build();
    }

    public TransactionDemoResult saveRestaurantAndThrowException(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (EXCEPTION_NO_TX)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        restaurantRepository.save(restaurant);
        throw new IllegalStateException("Exception after save without transaction");
    }

    @Transactional
    public TransactionDemoResult saveRestaurantAndThrowExceptionWithTransactional(RestaurantCreateRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName() + " (EXCEPTION_WITH_TX)")
                .city(request.getCity())
                .cuisineType(request.getCuisineType())
                .build();

        restaurantRepository.save(restaurant);
        throw new IllegalStateException("Exception after save with transaction");
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
