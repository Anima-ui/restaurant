package com.restaurant.app.repository;

import com.restaurant.app.domain.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantNativeSearchRepository {
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

    @Query(
            value = """
                    SELECT DISTINCT r
                    FROM Restaurant r
                    JOIN r.dishes d
                    WHERE (:city IS NULL OR LOWER(r.city) = :city)
                      AND (:cuisineType IS NULL OR LOWER(r.cuisineType) = :cuisineType)
                      AND (:dishNamePattern IS NULL OR LOWER(d.name) LIKE :dishNamePattern)
                      AND (:minDishPrice IS NULL OR d.price >= :minDishPrice)
                      AND (:maxDishPrice IS NULL OR d.price <= :maxDishPrice)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT r.id)
                    FROM Restaurant r
                    JOIN r.dishes d
                    WHERE (:city IS NULL OR LOWER(r.city) = :city)
                      AND (:cuisineType IS NULL OR LOWER(r.cuisineType) = :cuisineType)
                      AND (:dishNamePattern IS NULL OR LOWER(d.name) LIKE :dishNamePattern)
                      AND (:minDishPrice IS NULL OR d.price >= :minDishPrice)
                      AND (:maxDishPrice IS NULL OR d.price <= :maxDishPrice)
                    """
    )
    Page<Restaurant> searchByDishFiltersJpql(@Param("city") String city,
                                             @Param("cuisineType") String cuisineType,
                                             @Param("dishNamePattern") String dishNamePattern,
                                             @Param("minDishPrice") BigDecimal minDishPrice,
                                             @Param("maxDishPrice") BigDecimal maxDishPrice,
                                             Pageable pageable);
}
