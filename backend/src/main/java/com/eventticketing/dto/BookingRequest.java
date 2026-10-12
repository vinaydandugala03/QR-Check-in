package com.eventticketing.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    /**
     * Optional for backward compatibility.
     * The authenticated JWT UserPrincipal is always the authoritative source of ownership.
     */
    private Long userId;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> seatIds;

    public BookingRequest() {
    }

    public BookingRequest(Long eventId, List<Long> seatIds) {
        this.eventId = eventId;
        this.seatIds = seatIds;
    }

    public BookingRequest(Long userId, Long eventId, List<Long> seatIds) {
        this.userId = userId;
        this.eventId = eventId;
        this.seatIds = seatIds;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}