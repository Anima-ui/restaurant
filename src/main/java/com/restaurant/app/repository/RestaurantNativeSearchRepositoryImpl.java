package com.restaurant.app.repository;

import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
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
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RestaurantNativeSearchRepositoryImpl implements RestaurantNativeSearchRepository {

    private static final Map<String, String> SORT_COLUMNS = createSortColumns();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final String searchSql;

    private final String countSql;

    public RestaurantNativeSearchRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                                @Value("classpath:sql/restaurant-search-native.sql")
                                                Resource restaurantSearchNativeResource,
                                                @Value("classpath:sql/restaurant-search-native-count.sql")
                                                Resource restaurantSearchNativeCountResource) {
        this.jdbcTemplate = jdbcTemplate;
        this.searchSql = readSql(restaurantSearchNativeResource);
        this.countSql = readSql(restaurantSearchNativeCountResource);
    }

    public Page<RestaurantSearchResultDto> searchByDishFiltersNative(String city,
                                                                     String cuisineType,
                                                                     String dishNamePattern,
                                                                     BigDecimal minDishPrice,
                                                                     BigDecimal maxDishPrice,
                                                                     Pageable pageable) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("city", city, Types.VARCHAR)
                .addValue("cuisineType", cuisineType, Types.VARCHAR)
                .addValue("dishNamePattern", dishNamePattern, Types.VARCHAR)
                .addValue("minDishPrice", minDishPrice, Types.NUMERIC)
                .addValue("maxDishPrice", maxDishPrice, Types.NUMERIC)
                .addValue("limit", pageable.getPageSize(), Types.INTEGER)
                .addValue("offset", pageable.getOffset(), Types.BIGINT);

        List<RestaurantSearchResultDto> restaurants = jdbcTemplate.query(
                buildSearchQuery(pageable.getSort()),
                parameters,
                (resultSet, rowNum) -> RestaurantSearchResultDto.builder()
                        .id(resultSet.getLong("id"))
                        .name(resultSet.getString("name"))
                        .city(resultSet.getString("city"))
                        .cuisineType(resultSet.getString("cuisine_type"))
                        .build()
        );

        Long total = jdbcTemplate.queryForObject(countSql, parameters, Long.class);
        if (restaurants.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total == null ? 0 : total);
        }
        return new PageImpl<>(restaurants, pageable, total == null ? 0 : total);
    }

    @SuppressWarnings("java:S2077")
    private String buildSearchQuery(Sort sort) {
        return searchSql + buildOrderByClause(sort) + " LIMIT :limit OFFSET :offset";
    }

    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY filtered.id ASC";
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
                .orElse("filtered.id ASC");

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
        columns.put("id", "filtered.id");
        columns.put("name", "filtered.name");
        columns.put("city", "filtered.city");
        columns.put("cuisineType", "filtered.cuisine_type");
        return Map.copyOf(columns);
    }
}
