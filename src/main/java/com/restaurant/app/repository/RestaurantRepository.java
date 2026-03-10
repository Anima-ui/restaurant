package com.restaurant.app.repository;

import com.restaurant.app.domain.model.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCity(String city);

    @EntityGraph(attributePaths = {"tables", "dishes"})
    List<Restaurant> findDetailedByCity(String city);

    @Query("SELECT r FROM Restaurant r")
    List<Restaurant> findAllWithoutFetch();

    @Query("SELECT DISTINCT r FROM Restaurant r LEFT JOIN FETCH r.dishes")
    List<Restaurant> findAllWithDishesJoinFetch();

    @EntityGraph(attributePaths = {"tables", "tables.bookings"})
    @Query("SELECT r FROM Restaurant r")
    List<Restaurant> findAllWithTablesAndBookings();
}
