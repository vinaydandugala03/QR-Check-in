package com.eventticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eventticketing.entity.BookingSeat;

public interface BookingSeatRepository
        extends JpaRepository<BookingSeat, Long> {

    boolean existsBySeatIdAndBookingEventIdAndBookingStatus(
            Long seatId,
            Long eventId,
            String status);

    boolean existsBySeatIdAndBookingEventId(
            Long seatId,
            Long eventId);

    boolean existsBySeatId(Long seatId);

    long countByBookingEventIdAndBookingStatus(Long eventId, String status);

    @org.springframework.data.jpa.repository.Query(
        "SELECT DISTINCT bs.seat.id FROM BookingSeat bs WHERE bs.booking.event.id = :eventId AND bs.booking.status = :status AND bs.seat.id IS NOT NULL"
    )
    java.util.List<Long> findDistinctBookedSeatIdsByEventIdAndStatus(
            @org.springframework.data.repository.query.Param("eventId") Long eventId,
            @org.springframework.data.repository.query.Param("status") String status);

    java.util.List<BookingSeat> findByBookingEventIdAndBookingStatus(Long eventId, String status);

    java.util.List<BookingSeat> findByBookingId(Long bookingId);
}
