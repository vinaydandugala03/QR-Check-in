package com.eventticketing.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventticketing.entity.BookingSeat;
import com.eventticketing.service.BookingSeatService;

@RestController
@RequestMapping("/api/booking-seats")
public class BookingSeatController {

    private final BookingSeatService bookingSeatService;

    public BookingSeatController(BookingSeatService bookingSeatService) {
        this.bookingSeatService = bookingSeatService;
    }

    @PostMapping
    public BookingSeat createBookingSeat(
            @RequestBody BookingSeat bookingSeat) {

        return bookingSeatService.createBookingSeat(bookingSeat);
    }

    @GetMapping
    public List<BookingSeat> getAllBookingSeats() {
        return bookingSeatService.getAllBookingSeats();
    }
}