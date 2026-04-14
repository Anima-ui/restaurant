package com.restaurant.app.repository;

import com.restaurant.app.domain.model.Booking;
import com.restaurant.app.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCustomerId(Long customerId);

    @Query("""
            select coalesce(sum(b.guestCount), 0)
            from Booking b
            where b.table.id = :tableId
              and b.bookingTime = :bookingTime
              and b.status in :statuses
            """)
    long sumGuestCountByTableIdAndBookingTimeAndStatusIn(@Param("tableId") Long tableId,
                                                         @Param("bookingTime") LocalDateTime bookingTime,
                                                         @Param("statuses") Collection<BookingStatus> statuses);
}
