package com.eventticketing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.eventticketing.dto.TicketDtos.CheckInRequest;
import com.eventticketing.dto.TicketDtos.CheckInResponse;
import com.eventticketing.dto.TicketDtos.TicketResponse;
import com.eventticketing.entity.Booking;
import com.eventticketing.entity.Event;
import com.eventticketing.entity.Seat;
import com.eventticketing.entity.Ticket;
import com.eventticketing.entity.User;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.BookingSeatRepository;
import com.eventticketing.repository.EventRepository;
import com.eventticketing.repository.SeatRepository;
import com.eventticketing.repository.TicketRepository;
import com.eventticketing.repository.UserRepository;
import com.eventticketing.security.UserPrincipal;
import com.eventticketing.service.QrCodeService;
import com.eventticketing.service.TicketService;

/** Unit tests for QR check-in rules. They use mocks, so no database is needed. */
class TicketServiceTest {

    private TicketRepository tickets;
    private UserRepository users;
    private TicketService service;
    private UserPrincipal staff;

    @BeforeEach
    void setUp() {
        tickets = mock(TicketRepository.class);
        users = mock(UserRepository.class);
        service = new TicketService(tickets, mock(BookingRepository.class), mock(BookingSeatRepository.class),
                mock(SeatRepository.class), mock(EventRepository.class), users, new QrCodeService());
        staff = new UserPrincipal(99L, "gate@demo.com", "x", "GATE_STAFF");
    }

    private Ticket ticket(String code, long eventId, String bookingStatus, String eventStatus) {
        User user = new User();
        user.setId(1L);
        user.setName("Cathy");
        Event event = new Event();
        event.setId(eventId);
        event.setName("Indie Rock Night");
        event.setEventDate(LocalDate.now().plusDays(5));
        event.setStatus(eventStatus);
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setUser(user);
        booking.setEvent(event);
        booking.setStatus(bookingStatus);
        Seat seat = new Seat();
        seat.setId(5L);
        seat.setSeatNumber("A1");
        Ticket t = new Ticket();
        t.setTicketCode(code);
        t.setBooking(booking);
        t.setSeat(seat);
        t.setEvent(event);
        when(tickets.findByTicketCodeForUpdate(code)).thenReturn(Optional.of(t));
        return t;
    }

    @Test
    void validTicketChecksInOnceAndSecondScanIsRejected() {
        Ticket t = ticket("abc-123", 1L, "CONFIRMED", "UPCOMING");

        CheckInResponse first = service.checkIn(new CheckInRequest("abc-123", 1L), staff);
        assertEquals("SUCCESS", first.status());
        assertTrue(t.isCheckedIn());
        assertNotNull(t.getCheckedInAt());
        assertEquals(99L, t.getCheckedInBy());

        CheckInResponse second = service.checkIn(new CheckInRequest("abc-123", 1L), staff);
        assertEquals("ALREADY_CHECKED_IN", second.status());
    }

    @Test
    void unknownCodeIsInvalid() {
        when(tickets.findByTicketCodeForUpdate("nope")).thenReturn(Optional.empty());
        CheckInResponse r = service.checkIn(new CheckInRequest("nope", null), staff);
        assertEquals("INVALID", r.status());
        assertNull(r.ticket());
    }

    @Test
    void qrPrefixIsAccepted() {
        Ticket t = ticket("xyz-789", 1L, "CONFIRMED", "UPCOMING");
        CheckInResponse r = service.checkIn(new CheckInRequest("ETS:xyz-789", null), staff);
        assertEquals("SUCCESS", r.status());
        assertTrue(t.isCheckedIn());
    }

    @Test
    void ticketForAnotherEventIsRejected() {
        Ticket t = ticket("evt-1", 1L, "CONFIRMED", "UPCOMING");
        CheckInResponse r = service.checkIn(new CheckInRequest("evt-1", 2L), staff);
        assertEquals("WRONG_EVENT", r.status());
        assertFalse(t.isCheckedIn());
    }

    @Test
    void cancelledBookingOrEventIsNotValid() {
        Ticket a = ticket("can-1", 1L, "CANCELLED", "UPCOMING");
        assertEquals("NOT_VALID", service.checkIn(new CheckInRequest("can-1", null), staff).status());
        assertFalse(a.isCheckedIn());

        Ticket b = ticket("can-2", 1L, "CONFIRMED", "CANCELLED");
        assertEquals("NOT_VALID", service.checkIn(new CheckInRequest("can-2", null), staff).status());
        assertFalse(b.isCheckedIn());
    }

    @Test
    void ownerGetsTicketsWithQrImageAndOthersAreDenied() {
        Ticket t = ticket("own-1", 1L, "CONFIRMED", "UPCOMING");
        when(users.existsById(1L)).thenReturn(true);
        when(tickets.findByBookingUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(t));

        UserPrincipal owner = new UserPrincipal(1L, "customer@demo.com", "x", "USER");
        List<TicketResponse> list = service.getTicketsForUser(1L, owner);
        assertEquals(1, list.size());
        assertTrue(list.get(0).qrImage().startsWith("data:image/png;base64,"));
        assertEquals("A1", list.get(0).seatNumber());

        UserPrincipal stranger = new UserPrincipal(2L, "other@demo.com", "x", "USER");
        assertThrows(AccessDeniedException.class, () -> service.getTicketsForUser(1L, stranger));
    }
}
