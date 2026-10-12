package com.eventticketing.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {

    private Long bookingId;
    private Long userId;
    private String userName;
    private Long eventId;
    private String eventName;
    private LocalDateTime bookingDate;
    private Double totalAmount;
    private String status;
    private List<Long> seatIds;
    private List<String> seatNumbers;

    public BookingResponse() {
    }

    public BookingResponse(
            Long bookingId,
            Long userId,
            String userName,
            Long eventId,
            String eventName,
            LocalDateTime bookingDate,
            Double totalAmount,
            String status,
            List<Long> seatIds,
            List<String> seatNumbers) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.userName = userName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.seatIds = seatIds;
        this.seatNumbers = seatNumbers;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}

