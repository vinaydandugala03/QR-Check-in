package com.eventticketing.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

/** Request/response shapes for tickets, QR check-in and the dashboard. */
public final class TicketDtos {

    private TicketDtos() {
    }

    public record TicketResponse(
            Long ticketId,
            String ticketCode,
            String qrImage,
            Long bookingId,
            Long eventId,
            String eventName,
            LocalDate eventDate,
            LocalTime startTime,
            String venueName,
            String seatNumber,
            String attendeeName,
            boolean checkedIn,
            LocalDateTime checkedInAt) {
    }

    public record CheckInRequest(
            @NotBlank(message = "Ticket code is required") String ticketCode,
            Long eventId) {
    }

    /** status: SUCCESS, ALREADY_CHECKED_IN, INVALID, WRONG_EVENT or NOT_VALID. */
    public record CheckInResponse(String status, String message, TicketResponse ticket) {
    }

    public record EventSummary(
            Long eventId,
            String eventName,
            LocalDate eventDate,
            String status,
            long totalSeats,
            long bookedSeats,
            long availableSeats,
            long ticketsIssued,
            long checkedIn) {
    }

    public record DashboardResponse(
            long totalEvents,
            long totalUsers,
            long totalBookings,
            double revenue,
            long ticketsIssued,
            long checkedIn,
            List<EventSummary> events) {
    }
}
