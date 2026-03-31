package com.restaurant.app.sevice.cache;

import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.sevice.RestaurantSearchMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantSearchCacheKeyTest {

    @Test
    void ofCreatesEqualKeysAndHashCodeMatches() {
        RestaurantSearchRequest request = new RestaurantSearchRequest(
                "Moscow",
                "Italian",
                "Pasta",
                BigDecimal.TEN,
                new BigDecimal("30")
        );

        RestaurantSearchCacheKey first = RestaurantSearchCacheKey.of(
                RestaurantSearchMode.JPQL,
                request,
                0,
                10,
                "name:ASC"
        );
        RestaurantSearchCacheKey second = new RestaurantSearchCacheKey(
                RestaurantSearchMode.JPQL,
                "Moscow",
                "Italian",
                "Pasta",
                BigDecimal.TEN,
                new BigDecimal("30"),
                0,
                10,
                "name:ASC"
        );

        assertThat(first)
                .isEqualTo(second)
                .isEqualTo(first)
                .hasSameHashCodeAs(second)
                .isNotEqualTo(new Object())
                .isNotEqualTo(null)
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.NATIVE,
                        "Moscow",
                        "Italian",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        0,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Saint Petersburg",
                        "Italian",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        0,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Japanese",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        0,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Italian",
                        "Sushi",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        0,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Italian",
                        "Pasta",
                        BigDecimal.ONE,
                        new BigDecimal("30"),
                        0,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Italian",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("31"),
                        0,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Italian",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        1,
                        10,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Italian",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        0,
                        20,
                        "name:ASC"
                ))
                .isNotEqualTo(new RestaurantSearchCacheKey(
                        RestaurantSearchMode.JPQL,
                        "Moscow",
                        "Italian",
                        "Pasta",
                        BigDecimal.TEN,
                        new BigDecimal("30"),
                        0,
                        10,
                        "city:DESC"
                ));
    }
}
