package com.eventticketing.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventticketing.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    boolean existsById(Long id);

    List<Seat> findByVenueId(Long venueId);

    List<Seat> findByVenueIdAndSeatNumberIn(Long venueId, Collection<String> seatNumbers);

    @org.springframework.data.jpa.repository.Query(
        "SELECT s FROM Seat s WHERE s.venue.id = :venueId AND NOT EXISTS (" +
        "SELECT 1 FROM BookingSeat bs WHERE bs.seat.id = s.id AND bs.booking.event.id = :eventId AND bs.booking.status = 'CONFIRMED')"
    )
    List<Seat> findAvailableSeatsByVenueIdAndEventId(
            @org.springframework.data.repository.query.Param("venueId") Long venueId,
            @org.springframework.data.repository.query.Param("eventId") Long eventId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(s) FROM Seat s WHERE s.venue.id = :venueId AND NOT EXISTS (" +
        "SELECT 1 FROM BookingSeat bs WHERE bs.seat.id = s.id AND bs.booking.event.id = :eventId AND bs.booking.status = 'CONFIRMED')"
    )
    long countAvailableSeatsByVenueIdAndEventId(
            @org.springframework.data.repository.query.Param("venueId") Long venueId,
            @org.springframework.data.repository.query.Param("eventId") Long eventId);
}
