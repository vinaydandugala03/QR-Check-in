
package com.eventticketing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eventticketing.dto.BulkSeatCreationRequest;
import com.eventticketing.entity.Seat;
import com.eventticketing.service.SeatService;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping
    public Seat createSeat(@jakarta.validation.Valid @RequestBody Seat seat) {
        return seatService.createSeat(seat);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Seat> createSeatsBulk(@jakarta.validation.Valid @RequestBody BulkSeatCreationRequest request) {
        return seatService.createSeatsBulk(request);
    }

    @GetMapping
    public List<Seat> getAllSeats() {
        return seatService.getAllSeats();
    }

    @GetMapping("/{id}")
    public Seat getSeatById(@PathVariable Long id) {
        return seatService.getSeatById(id);
    }

    @PutMapping("/{id}")
    public Seat updateSeat(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Seat seat) {
        return seatService.updateSeat(id, seat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok("Seat deleted successfully");
    }
}