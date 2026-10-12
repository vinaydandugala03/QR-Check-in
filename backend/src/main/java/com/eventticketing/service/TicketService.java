package com.eventticketing.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventticketing.dto.TicketDtos.CheckInRequest;
import com.eventticketing.dto.TicketDtos.CheckInResponse;
import com.eventticketing.dto.TicketDtos.DashboardResponse;
import com.eventticketing.dto.TicketDtos.EventSummary;
import com.eventticketing.dto.TicketDtos.TicketResponse;
import com.eventticketing.entity.Booking;
import com.eventticketing.entity.Event;
import com.eventticketing.entity.Seat;
import com.eventticketing.entity.Ticket;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.BookingSeatRepository;
import com.eventticketing.repository.EventRepository;
import com.eventticketing.repository.SeatRepository;
import com.eventticketing.repository.TicketRepository;
import com.eventticketing.repository.UserRepository;
import com.eventticketing.security.UserPrincipal;

@Service
public class TicketService {

    /** Prefix inside the QR so the scanner can tell our codes from random QR codes. */
    public static final String QR_PREFIX = "ETS:";

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    public TicketService(
            TicketRepository ticketRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            SeatRepository seatRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            QrCodeService qrCodeService) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
    }

    /** Called inside the booking transaction: one unique ticket per seat. */
    @Transactional
    public List<Ticket> issueTickets(Booking booking, Collection<Seat> seats) {
        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seats) {
            Ticket t = new Ticket();
            t.setTicketCode(UUID.randomUUID().toString());
            t.setBooking(booking);
            t.setSeat(seat);
            t.setEvent(booking.getEvent());
            tickets.add(t);
        }
        return ticketRepository.saveAll(tickets);
    }

    /** Called when a booking is cancelled: its tickets stop existing, unless someone already used them. */
    @Transactional
    public void voidTickets(Long bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        if (tickets.stream().anyMatch(Ticket::isCheckedIn)) {
            throw new IllegalStateException("Tickets from this booking were already used at the event, so it cannot be cancelled");
        }
        ticketRepository.deleteAll(tickets);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsForUser(Long userId, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view tickets");
        }
        boolean isAdmin = "ADMIN".equalsIgnoreCase(principal.getRole());
        boolean isOwner = principal.getId() != null && principal.getId().equals(userId);
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Access denied: You can only view your own tickets");
        }
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found with ID: " + userId);
        }
        return ticketRepository.findByBookingUserIdOrderByCreatedAtDesc(userId).stream()
                .map(t -> toResponse(t, true))
                .toList();
    }

    /**
     * The backend decides everything here: the scanner only sends the text it read from the QR code.
     * The ticket row is locked while we check and update it, so a code can be accepted only once.
     */
    @Transactional
    public CheckInResponse checkIn(CheckInRequest request, UserPrincipal staff) {
        String code = normalize(request.ticketCode());
        Optional<Ticket> found = ticketRepository.findByTicketCodeForUpdate(code);
        if (found.isEmpty()) {
            return new CheckInResponse("INVALID", "Invalid ticket: this code is not recognised", null);
        }
        Ticket ticket = found.get();
        TicketResponse info = toResponse(ticket, false);

        if (request.eventId() != null && !request.eventId().equals(ticket.getEvent().getId())) {
            return new CheckInResponse("WRONG_EVENT",
                    "This ticket is for a different event: " + ticket.getEvent().getName(), info);
        }
        if ("CANCELLED".equalsIgnoreCase(ticket.getEvent().getStatus())) {
            return new CheckInResponse("NOT_VALID", "This event has been cancelled", info);
        }
        if (!"CONFIRMED".equals(ticket.getBooking().getStatus())) {
            return new CheckInResponse("NOT_VALID", "This booking is no longer valid", info);
        }
        if (ticket.isCheckedIn()) {
            return new CheckInResponse("ALREADY_CHECKED_IN",
                    "Already checked in at " + ticket.getCheckedInAt().toLocalTime().withNano(0), info);
        }
        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticket.setCheckedInBy(staff != null ? staff.getId() : null);
        ticketRepository.save(ticket);
        return new CheckInResponse("SUCCESS", "Checked in. Welcome, " + info.attendeeName() + "!", toResponse(ticket, false));
    }

    @Transactional(readOnly = true)
    public EventSummary eventSummary(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));
        return summarise(event);
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<EventSummary> events = eventRepository.findAll().stream().map(this::summarise).toList();
        return new DashboardResponse(
                eventRepository.count(),
                userRepository.count(),
                bookingRepository.count(),
                bookingRepository.sumConfirmedRevenue(),
                ticketRepository.count(),
                ticketRepository.countByCheckedInTrue(),
                events);
    }

    private EventSummary summarise(Event event) {
        long total = event.getVenue() == null ? 0 : seatRepository.findByVenueId(event.getVenue().getId()).size();
        long booked = bookingSeatRepository.countByBookingEventIdAndBookingStatus(event.getId(), "CONFIRMED");
        return new EventSummary(
                event.getId(), event.getName(), event.getEventDate(), event.getStatus(),
                total, booked, Math.max(0, total - booked),
                ticketRepository.countByEventId(event.getId()),
                ticketRepository.countByEventIdAndCheckedInTrue(event.getId()));
    }

    private String normalize(String raw) {
        String code = raw == null ? "" : raw.trim();
        return code.startsWith(QR_PREFIX) ? code.substring(QR_PREFIX.length()).trim() : code;
    }

    private TicketResponse toResponse(Ticket t, boolean withQr) {
        Event e = t.getEvent();
        return new TicketResponse(
                t.getId(),
                t.getTicketCode(),
                withQr ? qrCodeService.toDataUri(QR_PREFIX + t.getTicketCode()) : null,
                t.getBooking().getId(),
                e.getId(),
                e.getName(),
                e.getEventDate(),
                e.getStartTime(),
                e.getVenue() != null ? e.getVenue().getName() : null,
                t.getSeat().getSeatNumber(),
                t.getBooking().getUser() != null ? t.getBooking().getUser().getName() : null,
                t.isCheckedIn(),
                t.getCheckedInAt());
    }
}
