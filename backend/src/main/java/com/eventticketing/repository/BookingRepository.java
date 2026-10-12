package com.eventticketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventticketing.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    boolean existsByEventId(Long eventId);

    boolean existsByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    double sumConfirmedRevenue();
}