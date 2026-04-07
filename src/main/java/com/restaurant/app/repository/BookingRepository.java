package com.restaurant.app.repository;

import com.restaurant.app.domain.model.Booking;
import com.restaurant.app.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByTableIdAndBookingTimeAndStatusIn(Long tableId,
                                                     LocalDateTime bookingTime,
                                                     Collection<BookingStatus> statuses);
}
