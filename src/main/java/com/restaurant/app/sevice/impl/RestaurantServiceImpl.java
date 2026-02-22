package com.restaurant.app.sevice.impl;

import com.restaurant.app.domain.dto.RestaurantDto;
import com.restaurant.app.domain.model.Restaurant;
import com.restaurant.app.mapper.RestaurantMapper;
import com.restaurant.app.repository.RestaurantRepository;
import com.restaurant.app.sevice.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public RestaurantDto getById(Long id) {
        Restaurant restaurant = repository.findById(id).orElseThrow();
        return mapper.toDto(restaurant);
    }

    public List<RestaurantDto> getByCity(String city) {
        return repository.findByCity(city)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public RestaurantDto create(RestaurantDto dto) {
        Restaurant saved = repository.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
    }
}
