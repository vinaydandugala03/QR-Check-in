
package com.eventticketing.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.eventticketing.entity.Venue;
import com.eventticketing.repository.VenueRepository;
import com.eventticketing.repository.EventRepository;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public VenueService(
            VenueRepository venueRepository,
            EventRepository eventRepository) {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Venue not found with ID: " + id));
    }

    public Venue updateVenue(Long id, Venue updatedVenue) {
        Venue existingVenue = getVenueById(id);

        existingVenue.setName(updatedVenue.getName());
        existingVenue.setLocation(updatedVenue.getLocation());
        existingVenue.setCapacity(updatedVenue.getCapacity());

        return venueRepository.save(existingVenue);
    }

    public void deleteVenue(Long id) {
        Venue venue = getVenueById(id);

        if (eventRepository.existsByVenueId(id)) {
            throw new IllegalStateException(
                    "Cannot delete a venue that is assigned to events");
        }

        venueRepository.delete(venue);
    }
}