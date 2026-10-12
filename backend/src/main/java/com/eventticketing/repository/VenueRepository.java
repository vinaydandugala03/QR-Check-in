package com.eventticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eventticketing.entity.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}