package com.restaurant.app.repository;

import com.restaurant.app.domain.model.Dish;
import com.restaurant.app.domain.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:restaurant-repository;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@Transactional
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void clearData() {
        restaurantRepository.deleteAll();
    }

    @Test
    void searchByDishFiltersJpqlReturnsMatchingRestaurantPage() {
        createRestaurant(
                "Roma",
                "Moscow",
                "Italian",
                dish("Truffle Pasta", "39.90"),
                dish("Risotto", "22.50")
        );
        createRestaurant(
                "Tokyo",
                "Moscow",
                "Japanese",
                dish("Sushi Set", "18.00")
        );

        Page<Restaurant> result = restaurantRepository.searchByDishFiltersJpql(
                "moscow",
                "italian",
                "%pasta%",
                new BigDecimal("30.00"),
                new BigDecimal("45.00"),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Restaurant::getName)
                .containsExactly("Roma");
    }

    @Test
    void searchByDishFiltersNativeReturnsMatchingRestaurantPage() {
        createRestaurant(
                "Roma",
                "Moscow",
                "Italian",
                dish("Truffle Pasta", "39.90")
        );
        createRestaurant(
                "Burger House",
                "Moscow",
                "American",
                dish("Cheeseburger", "14.00")
        );

        Page<Restaurant> result = restaurantRepository.searchByDishFiltersNative(
                "moscow",
                null,
                "%burger%",
                null,
                new BigDecimal("20.00"),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Restaurant::getName)
                .containsExactly("Burger House");
    }

    @Test
    void searchByDishFiltersNativeSupportsSortingByName() {
        createRestaurant(
                "Roma",
                "Moscow",
                "Italian",
                dish("Truffle Pasta", "39.90")
        );
        createRestaurant(
                "Basilico",
                "Moscow",
                "Italian",
                dish("Cream Pasta", "24.00")
        );

        Page<Restaurant> result = restaurantRepository.searchByDishFiltersNative(
                "moscow",
                "italian",
                "%pasta%",
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertThat(result.getContent())
                .extracting(Restaurant::getName)
                .containsExactly("Basilico", "Roma");
    }

    private void createRestaurant(String name, String city, String cuisineType, Dish... dishes) {
        Restaurant restaurant = Restaurant.builder()
                .name(name)
                .city(city)
                .cuisineType(cuisineType)
                .build();
        for (Dish dish : dishes) {
            restaurant.addDish(dish);
        }
        restaurantRepository.saveAndFlush(restaurant);
    }

    private Dish dish(String name, String price) {
        return Dish.builder()
                .name(name)
                .price(new BigDecimal(price))
                .build();
    }
}
