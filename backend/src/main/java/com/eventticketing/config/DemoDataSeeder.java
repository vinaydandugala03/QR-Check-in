package com.eventticketing.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.eventticketing.entity.Event;
import com.eventticketing.entity.Seat;
import com.eventticketing.entity.Venue;
import com.eventticketing.repository.EventRepository;
import com.eventticketing.repository.SeatRepository;
import com.eventticketing.repository.VenueRepository;

/**
 * Adds sample venues, seats and events on first start (only when there are no events yet),
 * so the event list is never empty.
 */
@Component
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private final VenueRepository venues;
    private final SeatRepository seats;
    private final EventRepository events;

    public DemoDataSeeder(VenueRepository venues, SeatRepository seats, EventRepository events) {
        this.venues = venues;
        this.seats = seats;
        this.events = events;
    }

    @Override
    public void run(String... args) {
        if (events.count() > 0) {
            return;
        }
        Venue arena = venue("Grand Arena", "Banjara Hills, Hyderabad", 80);
        Venue hall = venue("Hitex Convention Hall", "Madhapur, Hyderabad", 60);
        addSeats(arena, 8, 10, 2, 1500.0, 700.0);
        addSeats(hall, 6, 10, 1, 1200.0, 500.0);

        LocalDate today = LocalDate.now();
        event("Indie Rock Night", "Three live bands, one stage, a night of original music.", "The Midnight Strings",
                "Music", today.plusDays(10), "19:00", "22:30", arena);
        event("Stand-up Comedy Special", "An evening of observational comedy with Hyderabad's best.", "Rahul Verma",
                "Comedy", today.plusDays(17), "20:00", "22:00", arena);
        event("Classical Theatre: The Last Act", "A gripping stage drama in Telugu and English.", "Natyam Theatre Group",
                "Theatre", today.plusDays(24), "18:30", "21:00", hall);
        event("Tech Conference 2026", "A full day of talks on cloud, AI and web engineering.", "Various speakers",
                "Conference", today.plusDays(31), "09:30", "17:30", hall);
    }

    private Venue venue(String name, String location, int capacity) {
        Venue v = new Venue();
        v.setName(name);
        v.setLocation(location);
        v.setCapacity(capacity);
        return venues.save(v);
    }

    /** Rows A.. with the first {@code premiumRows} rows PREMIUM (VIP section) and the rest REGULAR. */
    private void addSeats(Venue v, int rows, int perRow, int premiumRows, double premiumPrice, double regularPrice) {
        List<Seat> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            boolean premium = r < premiumRows;
            for (int n = 1; n <= perRow; n++) {
                String number = String.valueOf((char) ('A' + r)) + n;
                list.add(new Seat(null, number, premium ? "VIP" : "General", premium ? "PREMIUM" : "REGULAR",
                        premium ? premiumPrice : regularPrice, v));
            }
        }
        seats.saveAll(list);
    }

    private void event(String name, String description, String artist, String category, LocalDate date,
            String start, String end, Venue v) {
        Event e = new Event();
        e.setName(name);
        e.setDescription(description);
        e.setArtist(artist);
        e.setCategory(category);
        e.setEventDate(date);
        e.setStartTime(LocalTime.parse(start));
        e.setEndTime(LocalTime.parse(end));
        e.setStatus("UPCOMING");
        e.setVenue(v);
        events.save(e);
    }
}
