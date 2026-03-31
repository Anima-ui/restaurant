package com.restaurant.app.sevice.cache;

import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantSearchCacheTest {

    @Test
    void putGetClearAndSizeWorkAsExpected() {
        RestaurantSearchCache cache = new RestaurantSearchCache();
        RestaurantSearchCacheKey key = new RestaurantSearchCacheKey(
                null,
                "Moscow",
                "Italian",
                "pasta",
                null,
                null,
                0,
                10,
                "name:ASC"
        );
        PageImpl<RestaurantSearchResultDto> page = new PageImpl<>(List.of(
                RestaurantSearchResultDto.builder().id(1L).name("Roma").build()
        ));

        assertThat(cache.get(key)).isEmpty();
        assertThat(cache.size()).isZero();

        cache.put(key, page);

        assertThat(cache.get(key)).contains(page);
        assertThat(cache.size()).isEqualTo(1);

        cache.clear();

        assertThat(cache.get(key)).isEmpty();
        assertThat(cache.size()).isZero();
    }
}
