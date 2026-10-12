package com.eventticketing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventticketing.dto.BulkSeatCreationRequest;
import com.eventticketing.entity.Seat;
import com.eventticketing.entity.Venue;
import com.eventticketing.repository.BookingSeatRepository;
import com.eventticketing.repository.SeatRepository;
import com.eventticketing.repository.VenueRepository;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final VenueRepository venueRepository;

    public SeatService(
            SeatRepository seatRepository,
            BookingSeatRepository bookingSeatRepository,
            VenueRepository venueRepository) {
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.venueRepository = venueRepository;
    }

    public Seat createSeat(Seat seat) {
        if (seat.getVenue() == null || seat.getVenue().getId() == null) {
            throw new IllegalArgumentException("Seat must have a valid venue");
        }

        Venue venue = venueRepository.findById(seat.getVenue().getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Venue not found with ID: " + seat.getVenue().getId()));
        seat.setVenue(venue);

        return seatRepository.save(seat);
    }

    @Transactional
    public List<Seat> createSeatsBulk(BulkSeatCreationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Bulk seat creation request cannot be null");
        }
        if (request.getVenueId() == null) {
            throw new IllegalArgumentException("Venue ID is required");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (request.getRowFrom() == null || request.getRowFrom().trim().isEmpty()) {
            throw new IllegalArgumentException("rowFrom is required");
        }
        if (request.getRowTo() == null || request.getRowTo().trim().isEmpty()) {
            throw new IllegalArgumentException("rowTo is required");
        }
        if (request.getSeatsPerRow() == null || request.getSeatsPerRow() <= 0) {
            throw new IllegalArgumentException("seatsPerRow must be greater than zero");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("price must be greater than zero");
        }

        String rowFromStr = request.getRowFrom().trim().toUpperCase();
        String rowToStr = request.getRowTo().trim().toUpperCase();

        if (rowFromStr.length() != 1 || rowFromStr.charAt(0) < 'A' || rowFromStr.charAt(0) > 'Z') {
            throw new IllegalArgumentException("rowFrom must be a single letter from A to Z");
        }
        if (rowToStr.length() != 1 || rowToStr.charAt(0) < 'A' || rowToStr.charAt(0) > 'Z') {
            throw new IllegalArgumentException("rowTo must be a single letter from A to Z");
        }

        char startChar = rowFromStr.charAt(0);
        char endChar = rowToStr.charAt(0);

        if (startChar > endChar) {
            throw new IllegalArgumentException("rowFrom ('" + startChar + "') cannot be greater than rowTo ('" + endChar + "')");
        }

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Venue not found with ID: " + request.getVenueId()));

        String normalizedCategory = request.getCategory().trim();

        List<String> generatedSeatNumbers = new ArrayList<>();
        List<Seat> seatsToCreate = new ArrayList<>();

        for (char row = startChar; row <= endChar; row++) {
            for (int number = 1; number <= request.getSeatsPerRow(); number++) {
                String seatNumber = "" + row + number;
                generatedSeatNumbers.add(seatNumber);

                Seat seat = new Seat();
                seat.setVenue(venue);
                seat.setSeatNumber(seatNumber);
                seat.setSection(normalizedCategory);
                seat.setSeatType(normalizedCategory);
                seat.setPrice(request.getPrice());
                seatsToCreate.add(seat);
            }
        }

        // Check for conflicting seats already in this venue
        List<Seat> existingConflictingSeats = seatRepository.findByVenueIdAndSeatNumberIn(
                venue.getId(),
                generatedSeatNumbers
        );

        if (!existingConflictingSeats.isEmpty()) {
            List<String> conflictingNumbers = existingConflictingSeats.stream()
                    .map(Seat::getSeatNumber)
                    .distinct()
                    .collect(Collectors.toList());
            throw new IllegalStateException(
                    "Seats already exist for venue " + venue.getId() + ": " + conflictingNumbers
            );
        }

        return seatRepository.saveAll(seatsToCreate);
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public Seat getSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Seat not found with ID: " + id));
    }

    public Seat updateSeat(Long id, Seat updatedSeat) {
        Seat existingSeat = getSeatById(id);

        if (updatedSeat.getVenue() != null && updatedSeat.getVenue().getId() != null) {
            Venue venue = venueRepository.findById(updatedSeat.getVenue().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Venue not found with ID: " + updatedSeat.getVenue().getId()));
            existingSeat.setVenue(venue);
        }

        existingSeat.setSeatNumber(updatedSeat.getSeatNumber());
        existingSeat.setSection(updatedSeat.getSection());
        existingSeat.setSeatType(updatedSeat.getSeatType());
        existingSeat.setPrice(updatedSeat.getPrice());

        return seatRepository.save(existingSeat);
    }

    public void deleteSeat(Long id) {
        Seat seat = getSeatById(id);

        if (bookingSeatRepository.existsBySeatId(id)) {
            throw new IllegalStateException(
                    "Cannot delete a seat referenced by booking records");
        }

        seatRepository.delete(seat);
    }
}