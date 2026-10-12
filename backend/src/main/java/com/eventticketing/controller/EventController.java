
package com.eventticketing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventticketing.entity.Event;
import com.eventticketing.repository.BookingSeatRepository;
import com.eventticketing.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final BookingSeatRepository bookingSeatRepository;

    public EventController(EventService eventService, BookingSeatRepository bookingSeatRepository) {
        this.eventService = eventService;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    /** Public: ids of seats already booked (CONFIRMED) for this event, used by the seat map. */
    @GetMapping("/{id}/booked-seats")
    public List<Long> getBookedSeatIds(@PathVariable Long id) {
        return bookingSeatRepository.findDistinctBookedSeatIdsByEventIdAndStatus(id, "CONFIRMED");
    }

    @PostMapping
    public Event createEvent(@jakarta.validation.Valid @RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PutMapping("/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Event event) {
        return eventService.updateEvent(id, event);
    }
@DeleteMapping("/{id}")
public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
    eventService.deleteEvent(id);
    return ResponseEntity.ok("Event deleted successfully");
}
}