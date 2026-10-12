package com.eventticketing.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventticketing.dto.BookingRequest;
import com.eventticketing.dto.BookingResponse;
import com.eventticketing.entity.Booking;
import com.eventticketing.entity.BookingSeat;
import com.eventticketing.entity.Event;
import com.eventticketing.entity.Seat;
import com.eventticketing.entity.User;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.BookingSeatRepository;
import com.eventticketing.repository.EventRepository;
import com.eventticketing.repository.SeatRepository;
import com.eventticketing.repository.UserRepository;
import com.eventticketing.security.UserPrincipal;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketService ticketService;

    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository,
            TicketService ticketService) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.ticketService = ticketService;
    }

    // Get all bookings (Admin use)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toBookingResponse)
                .toList();
    }

    // Get booking history for a specific user with service-level ownership validation
    public List<BookingResponse> getBookingsByUserId(Long userId, UserPrincipal principal) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view bookings");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.getRole());
        boolean isOwner = principal.getId() != null && principal.getId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Access denied: You can only access your own bookings");
        }

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found with ID: " + userId);
        }

        return bookingRepository.findByUserId(userId).stream()
                .map(this::toBookingResponse)
                .toList();
    }

    // Check whether a seat is currently booked for an event
    public boolean isSeatBooked(Long seatId, Long eventId) {
        if (seatId == null || eventId == null) {
            throw new IllegalArgumentException("Seat ID and event ID are required");
        }

        return bookingSeatRepository
                .existsBySeatIdAndBookingEventIdAndBookingStatus(seatId, eventId, "CONFIRMED");
    }

    // Create a booking with authenticated user enforcement and server-side price calculation
    @Transactional
    public BookingResponse createBooking(BookingRequest request, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to create a booking");
        }

        if (request == null
                || request.getEventId() == null
                || request.getSeatIds() == null
                || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Event and at least one seat are required");
        }

        // Validate seat selection for uniqueness
        Set<Long> uniqueSeatIds = new HashSet<>(request.getSeatIds());
        if (uniqueSeatIds.size() != request.getSeatIds().size() || uniqueSeatIds.contains(null)) {
            throw new IllegalArgumentException("Seat selection contains duplicate or invalid IDs");
        }

        // Authoritative user identification from authenticated principal
        Long authenticatedUserId = principal.getId();
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + authenticatedUserId));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + request.getEventId()));

        if (event.getVenue() == null) {
            throw new IllegalStateException("Event does not have an assigned venue");
        }

        // Retrieve and validate requested seats
        List<Seat> seats = seatRepository.findAllById(uniqueSeatIds);
        if (seats.size() != uniqueSeatIds.size()) {
            throw new NoSuchElementException("One or more selected seats do not exist");
        }

        double totalAmount = 0.0;

        for (Seat seat : seats) {
            if (seat.getVenue() == null || !seat.getVenue().getId().equals(event.getVenue().getId())) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getSeatNumber() + " does not belong to this event's venue");
            }

            if (seat.getPrice() == null || seat.getPrice() < 0) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " has an invalid price");
            }

            boolean alreadyBooked = bookingSeatRepository
                    .existsBySeatIdAndBookingEventIdAndBookingStatus(seat.getId(), event.getId(), "CONFIRMED");

            if (alreadyBooked) {
                throw new IllegalStateException(
                        "Seat " + seat.getSeatNumber() + " is already booked for this event");
            }

            totalAmount += seat.getPrice();
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setBookingDate(LocalDateTime.now());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);

        // Save seat associations
        List<BookingSeat> bookingSeats = new ArrayList<>();
        List<String> seatNumbers = new ArrayList<>();
        for (Seat seat : seats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(savedBooking);
            bookingSeat.setSeat(seat);
            bookingSeats.add(bookingSeat);
            seatNumbers.add(seat.getSeatNumber());
        }

        bookingSeatRepository.saveAll(bookingSeats);
        ticketService.issueTickets(savedBooking, seats);

        return new BookingResponse(
                savedBooking.getId(),
                user.getId(),
                user.getName(),
                event.getId(),
                event.getName(),
                savedBooking.getBookingDate(),
                savedBooking.getTotalAmount(),
                savedBooking.getStatus(),
                new ArrayList<>(uniqueSeatIds),
                seatNumbers
        );
    }

    // Cancel a confirmed booking with service-level ownership validation
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, UserPrincipal principal) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        if (principal == null) {
            throw new AccessDeniedException("Authentication required to cancel a booking");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.getRole());
        boolean isOwner = booking.getUser() != null
                && principal.getId() != null
                && principal.getId().equals(booking.getUser().getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Access denied: You can only cancel your own bookings");
        }

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        ticketService.voidTickets(bookingId);
        booking.setStatus("CANCELLED");
        Booking updatedBooking = bookingRepository.save(booking);

        return toBookingResponse(updatedBooking);
    }

    public BookingResponse toBookingResponse(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        List<Long> seatIds = bookingSeats.stream()
                .map(bs -> bs.getSeat().getId())
                .toList();
        List<String> seatNumbers = bookingSeats.stream()
                .map(bs -> bs.getSeat().getSeatNumber())
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getUser() != null ? booking.getUser().getId() : null,
                booking.getUser() != null ? booking.getUser().getName() : null,
                booking.getEvent() != null ? booking.getEvent().getId() : null,
                booking.getEvent() != null ? booking.getEvent().getName() : null,
                booking.getBookingDate(),
                booking.getTotalAmount(),
                booking.getStatus(),
                seatIds,
                seatNumbers
        );
    }
}