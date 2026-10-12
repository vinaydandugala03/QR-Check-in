package com.eventticketing.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.eventticketing.entity.Event;
import com.eventticketing.entity.Venue;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.EventRepository;
import com.eventticketing.repository.VenueRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;

    public EventService(
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.venueRepository = venueRepository;
    }

    public Event createEvent(Event event) {
        if (event.getVenue() == null || event.getVenue().getId() == null) {
            throw new IllegalArgumentException("Event must have a valid venue");
        }

        Venue venue = venueRepository.findById(event.getVenue().getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Venue not found with ID: " + event.getVenue().getId()));
        event.setVenue(venue);

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Event not found with ID: " + id));
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        Event existingEvent = getEventById(id);

        if (updatedEvent.getVenue() != null && updatedEvent.getVenue().getId() != null) {
            Venue venue = venueRepository.findById(updatedEvent.getVenue().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Venue not found with ID: " + updatedEvent.getVenue().getId()));
            existingEvent.setVenue(venue);
        }

        existingEvent.setName(updatedEvent.getName());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setArtist(updatedEvent.getArtist());
        existingEvent.setCategory(updatedEvent.getCategory());
        existingEvent.setEventDate(updatedEvent.getEventDate());
        existingEvent.setStartTime(updatedEvent.getStartTime());
        existingEvent.setEndTime(updatedEvent.getEndTime());
        existingEvent.setStatus(updatedEvent.getStatus());

        return eventRepository.save(existingEvent);
    }

    public void deleteEvent(Long id) {
        Event event = getEventById(id);

        if (bookingRepository.existsByEventId(id)) {
            throw new IllegalStateException(
                    "Cannot delete an event that has booking history");
        }

        eventRepository.delete(event);
    }
}