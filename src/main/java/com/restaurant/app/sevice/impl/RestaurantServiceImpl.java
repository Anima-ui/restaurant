package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantSearchRequest;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.exception.ResourceNotFoundException;
import com.restaurant.app.mapper.RestaurantMapper;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.sevice.RestaurantSearchMode;
import com.restaurant.app.sevice.RestaurantService;
import com.restaurant.app.sevice.cache.RestaurantSearchCache;
import com.restaurant.app.sevice.cache.RestaurantSearchCacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private static final String RESTAURANT_NOT_FOUND_PREFIX = "Restaurant with id=";

    private static final String NOT_FOUND_SUFFIX = " was not found";

    private final RestaurantRepository repository;

    private final RestaurantMapper mapper;

    private final RestaurantSearchCache restaurantSearchCache;

    @Autowired
    public RestaurantServiceImpl(RestaurantRepository repository,
                                 RestaurantMapper mapper,
                                 RestaurantSearchCache restaurantSearchCache) {
        this.repository = repository;
        this.mapper = mapper;
        this.restaurantSearchCache = restaurantSearchCache;
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantDto getById(Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX));
        return mapper.toDto(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> getByCity(String city) {
        return repository.findByCity(city)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> getDetailedByCity(String city) {
        return repository.findDetailedByCity(city)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<RestaurantDto> searchByDishFiltersJpql(RestaurantSearchRequest request, Pageable pageable) {
        return searchWithCache(RestaurantSearchMode.JPQL, request, pageable);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantDto> searchByDishFiltersNative(RestaurantSearchRequest request, Pageable pageable) {
        return searchWithCache(RestaurantSearchMode.NATIVE, request, pageable);
    }

    @Transactional
    public RestaurantDto create(RestaurantCreateRequest dto) {
        Restaurant saved = repository.save(mapper.toEntity(dto));
        restaurantSearchCache.clear();
        return mapper.toDto(saved);
    }

    @Transactional
    public RestaurantDto update(Long id, RestaurantUpdateRequest dto) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX));
        restaurant.setName(dto.getName());
        restaurant.setCity(dto.getCity());
        restaurant.setCuisineType(dto.getCuisineType());
        Restaurant saved = repository.save(restaurant);
        restaurantSearchCache.clear();
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND_PREFIX + id + NOT_FOUND_SUFFIX));
        repository.delete(restaurant);
        restaurantSearchCache.clear();
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> findAllRestaurantsDishesWithNPlusProblem() {
        List<Restaurant> restaurants = repository.findAllWithoutFetch();
        restaurants.forEach(restaurant -> restaurant.getDishes().size());
        return restaurants.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> findAllRestaurantsDishesOptimized() {
        List<Restaurant> restaurants = repository.findAllWithDishesJoinFetch();
        restaurants.forEach(restaurant -> restaurant.getDishes().size());
        return restaurants.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> findAllTablesWithBookingsNPlusProblem() {
        List<Restaurant> restaurants = repository.findAllWithoutFetch();
        restaurants.forEach(restaurant -> restaurant.getTables().forEach(table -> table.getBookings().size()));
        return restaurants.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantDto> findAllTablesWithBookingsOptimized() {
        List<Restaurant> restaurants = repository.findAllWithTablesAndBookings();
        restaurants.forEach(restaurant -> restaurant.getTables().forEach(table -> table.getBookings().size()));
        return restaurants.stream().map(mapper::toDto).toList();
    }

    private Page<RestaurantDto> searchWithCache(RestaurantSearchMode mode,
                                                RestaurantSearchRequest request,
                                                Pageable pageable) {
        RestaurantSearchCacheKey key = RestaurantSearchCacheKey.of(
                mode,
                request,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                request.sortDescription(pageable.getSort())
        );

        return restaurantSearchCache.get(key)
                .orElseGet(() -> {
                    Page<RestaurantDto> result = executeSearch(mode, request, pageable)
                            .map(mapper::toDto);
                    restaurantSearchCache.put(key, result);
                    return result;
                });
    }

    private Page<Restaurant> executeSearch(RestaurantSearchMode mode,
                                           RestaurantSearchRequest request,
                                           Pageable pageable) {
        String city = normalizeForCaseInsensitiveEquals(request.city());
        String cuisineType = normalizeForCaseInsensitiveEquals(request.cuisineType());
        String dishNamePattern = normalizeForContainsSearch(request.dishName());

        if (mode == RestaurantSearchMode.NATIVE) {
            return repository.searchByDishFiltersNative(
                    city,
                    cuisineType,
                    dishNamePattern,
                    request.minDishPrice(),
                    request.maxDishPrice(),
                    pageable
            );
        }

        return repository.searchByDishFiltersJpql(
                city,
                cuisineType,
                dishNamePattern,
                request.minDishPrice(),
                request.maxDishPrice(),
                pageable
        );
    }

    private String normalizeForCaseInsensitiveEquals(String value) {
        return value == null ? null : value.toLowerCase();
    }

    private String normalizeForContainsSearch(String value) {
        return value == null ? null : "%" + value.toLowerCase() + "%";
    }
}
