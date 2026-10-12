package com.eventticketing.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eventticketing.dto.TicketDtos.CheckInRequest;
import com.eventticketing.dto.TicketDtos.CheckInResponse;
import com.eventticketing.dto.TicketDtos.DashboardResponse;
import com.eventticketing.dto.TicketDtos.EventSummary;
import com.eventticketing.dto.TicketDtos.TicketResponse;
import com.eventticketing.security.UserPrincipal;
import com.eventticketing.service.TicketService;

import jakarta.validation.Valid;

@RestController
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** A user's QR tickets (owner or ADMIN). */
    @GetMapping("/api/tickets/user/{userId}")
    public List<TicketResponse> myTickets(@PathVariable Long userId, @AuthenticationPrincipal UserPrincipal principal) {
        return ticketService.getTicketsForUser(userId, principal);
    }

    /** QR check-in. Only ADMIN and GATE_STAFF (enforced in SecurityConfig). */
    @PostMapping("/api/checkin")
    public CheckInResponse checkIn(@Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ticketService.checkIn(request, principal);
    }

    /** Totals and per-event attendance. Only ADMIN and GATE_STAFF. */
    @GetMapping("/api/dashboard")
    public DashboardResponse dashboard() {
        return ticketService.dashboard();
    }

    /** Public: seats, bookings and check-in counts for one event (shows "available seats"). */
    @GetMapping("/api/events/{id}/summary")
    public EventSummary eventSummary(@PathVariable Long id) {
        return ticketService.eventSummary(id);
    }
}
