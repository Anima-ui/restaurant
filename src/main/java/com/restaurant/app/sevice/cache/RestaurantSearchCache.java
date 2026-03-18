package com.restaurant.app.sevice.cache;

import com.restaurant.app.domain.dto.RestaurantDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RestaurantSearchCache {

    private final Map<RestaurantSearchCacheKey, Page<RestaurantDto>> cache = new HashMap<>();

    public synchronized Optional<Page<RestaurantDto>> get(RestaurantSearchCacheKey key) {
        return Optional.ofNullable(cache.get(key));
    }

    public synchronized void put(RestaurantSearchCacheKey key, Page<RestaurantDto> value) {
        cache.put(key, value);
    }

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized int size() {
        return cache.size();
    }
}
