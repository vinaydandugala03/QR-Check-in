package com.eventticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventticketing.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByVenueId(Long venueId);
}