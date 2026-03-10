package com.restaurant.app.sevice;

import com.restaurant.app.domain.dto.BookingCreateRequest;
import com.restaurant.app.domain.dto.BookingDto;
import com.restaurant.app.domain.model.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateRequest request);

    List<BookingDto> getAll();

    BookingDto getById(Long id);

    BookingDto updateStatus(Long id, BookingStatus status);

    List<BookingStatus> getAvailableStatuses();
}
