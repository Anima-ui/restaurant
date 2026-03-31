package com.restaurant.app.sevice.cache;

import com.restaurant.app.domain.dto.RestaurantSearchResultDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RestaurantSearchCache {

    private final Map<RestaurantSearchCacheKey, Page<RestaurantSearchResultDto>> cache = new HashMap<>();

    public  Optional<Page<RestaurantSearchResultDto>> get(RestaurantSearchCacheKey key) {
        return Optional.ofNullable(cache.get(key));
    }

    public  void put(RestaurantSearchCacheKey key, Page<RestaurantSearchResultDto> value) {
        cache.put(key, value);
    }

    public  void clear() {
        cache.clear();
    }

    public  int size() {
        return cache.size();
    }
}
