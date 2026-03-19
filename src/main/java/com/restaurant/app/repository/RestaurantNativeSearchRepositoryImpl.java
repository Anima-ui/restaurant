package com.restaurant.app.repository;

import com.restaurant.app.domain.model.Restaurant;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Repository
public class RestaurantNativeSearchRepositoryImpl implements RestaurantNativeSearchRepository {

    private static final Map<String, String> SORT_COLUMNS = createSortColumns();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final EntityManager entityManager;

    private final String searchSql;

    private final String countSql;

    public RestaurantNativeSearchRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                                EntityManager entityManager,
                                                @Value("classpath:sql/restaurant-search-native.sql")
                                                Resource restaurantSearchNativeResource,
                                                @Value("classpath:sql/restaurant-search-native-count.sql")
                                                Resource restaurantSearchNativeCountResource) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
        this.searchSql = readSql(restaurantSearchNativeResource);
        this.countSql = readSql(restaurantSearchNativeCountResource);
    }

    public Page<Restaurant> searchByDishFiltersNative(String city,
                                                      String cuisineType,
                                                      String dishNamePattern,
                                                      BigDecimal minDishPrice,
                                                      BigDecimal maxDishPrice,
                                                      Pageable pageable) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("city", city)
                .addValue("cuisineType", cuisineType)
                .addValue("dishNamePattern", dishNamePattern)
                .addValue("minDishPrice", minDishPrice)
                .addValue("maxDishPrice", maxDishPrice)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        List<Long> restaurantIds = jdbcTemplate.queryForList(
                searchSql + buildOrderByClause(pageable.getSort()) + " LIMIT :limit OFFSET :offset",
                parameters,
                Long.class
        );

        Long total = jdbcTemplate.queryForObject(countSql, parameters, Long.class);
        if (restaurantIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total == null ? 0 : total);
        }

        List<Restaurant> restaurants = entityManager.createQuery(
                        "SELECT r FROM Restaurant r WHERE r.id IN :ids", Restaurant.class)
                .setParameter("ids", restaurantIds)
                .getResultList();

        Map<Long, Restaurant> restaurantsById = restaurants.stream()
                .collect(java.util.stream.Collectors.toMap(Restaurant::getId, Function.identity()));
        List<Restaurant> orderedRestaurants = restaurantIds.stream()
                .map(restaurantsById::get)
                .toList();

        return new PageImpl<>(orderedRestaurants, pageable, total == null ? 0 : total);
    }

    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY r.id ASC";
        }

        String orderBy = sort.stream()
                .map(order -> {
                    String column = SORT_COLUMNS.get(order.getProperty());
                    if (column == null) {
                        throw new IllegalArgumentException("Unsupported sort field: " + order.getProperty());
                    }
                    return column + " " + order.getDirection().name();
                })
                .reduce((left, right) -> left + ", " + right)
                .orElse("r.id ASC");

        return " ORDER BY " + orderBy;
    }

    private String readSql(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read SQL resource: " + resource.getDescription(), exception);
        }
    }

    private static Map<String, String> createSortColumns() {
        Map<String, String> columns = new HashMap<>();
        columns.put("id", "r.id");
        columns.put("name", "r.name");
        columns.put("city", "r.city");
        columns.put("cuisineType", "r.cuisine_type");
        return Map.copyOf(columns);
    }
}
