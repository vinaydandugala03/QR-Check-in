package com.eventticketing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eventticketing.entity.Booking;
import com.eventticketing.entity.BookingSeat;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.BookingSeatRepository;

@Service
public class BookingSeatService {

    private final BookingSeatRepository bookingSeatRepository;
    private final BookingRepository bookingRepository;

    public BookingSeatService(
            BookingSeatRepository bookingSeatRepository,
            BookingRepository bookingRepository) {

        this.bookingSeatRepository = bookingSeatRepository;
        this.bookingRepository = bookingRepository;
    }

    public BookingSeat createBookingSeat(BookingSeat bookingSeat) {

        Long bookingId = bookingSeat.getBooking().getId();
        Long seatId = bookingSeat.getSeat().getId();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalStateException("Booking not found"));

        Long eventId = booking.getEvent().getId();

        boolean alreadyBooked =
                bookingSeatRepository
                        .existsBySeatIdAndBookingEventId(seatId, eventId);

        if (alreadyBooked) {
            throw new IllegalStateException(
                    "Seat is already booked for this event"
            );
        }

        bookingSeat.setBooking(booking);

        return bookingSeatRepository.save(bookingSeat);
    }

    public List<BookingSeat> getAllBookingSeats() {
        return bookingSeatRepository.findAll();
    }
}