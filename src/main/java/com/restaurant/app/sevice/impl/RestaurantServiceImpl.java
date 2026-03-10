package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RestaurantCreateRequest;
import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.dto.RestaurantUpdateRequest;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.mapper.RestaurantMapper;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.sevice.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository repository;

    private final RestaurantMapper mapper;

    @Autowired
    public RestaurantServiceImpl(RestaurantRepository repository, RestaurantMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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
        Restaurant restaurant = repository.findById(id).orElseThrow();
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

    @Transactional
    public RestaurantDto create(RestaurantCreateRequest dto) {
        Restaurant saved = repository.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
    }

    @Transactional
    public RestaurantDto update(Long id, RestaurantUpdateRequest dto) {
        Restaurant restaurant = repository.findById(id).orElseThrow();
        restaurant.setName(dto.getName());
        restaurant.setCity(dto.getCity());
        restaurant.setCuisineType(dto.getCuisineType());
        return mapper.toDto(repository.save(restaurant));
    }

    @Transactional
    public void delete(Long id) {
        Restaurant restaurant = repository.findById(id).orElseThrow();
        repository.delete(restaurant);
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
}
