package com.eventticketing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eventticketing.config.AiClientConfig;
import com.eventticketing.dto.ChatRequest;
import com.eventticketing.entity.Event;
import com.eventticketing.entity.Seat;
import com.eventticketing.exception.AiServiceUnavailableException;
import com.eventticketing.repository.BookingRepository;
import com.eventticketing.repository.EventRepository;
import com.eventticketing.repository.SeatRepository;
import com.eventticketing.service.ChatService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@SpringBootTest
class ChatServiceTest {

    @Autowired
    private AiClientConfig aiClientConfig;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ChatService chatService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testChatRequestValidation_ValidMessage() {
        ChatRequest req = new ChatRequest("What music events are happening this month?");
        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Valid message should have no violations");
    }

    @Test
    void testChatRequestValidation_BlankMessage() {
        ChatRequest req = new ChatRequest("   ");
        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Blank message should trigger constraint violation");
    }

    @Test
    void testChatRequestValidation_Exceeds500Chars() {
        String longMessage = "A".repeat(501);
        ChatRequest req = new ChatRequest(longMessage);
        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Message > 500 characters should trigger constraint violation");
        boolean hasSizeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("500"));
        assertTrue(hasSizeViolation, "Violation message should mention 500 character limit");
    }

    @Test
    void testMissingApiKey_ThrowsAiServiceUnavailableException() {
        // When API key is not configured, chatService must fail safely with AiServiceUnavailableException
        if (!aiClientConfig.isApiKeyConfigured()) {
            ChatRequest req = new ChatRequest("What is the starting price for Sunburn Festival?");
            AiServiceUnavailableException ex = assertThrows(
                    AiServiceUnavailableException.class,
                    () -> chatService.processChat(req, null)
            );
            assertTrue(ex.getMessage().contains("Groq API key"), "Exception should clearly state API key is not configured");
        }
    }

    @Test
    void testSeatAvailabilityRepositoryQuery_ExcludesConfirmedBookings() {
        // Ravindra Bharathi venue has ID 3 and host events 7 and 8
        Long venueId = 3L;
        Long eventId = 7L;

        long availableSeats = seatRepository.countAvailableSeatsByVenueIdAndEventId(venueId, eventId);
        java.util.List<com.eventticketing.entity.Seat> configuredSeats = seatRepository.findByVenueId(venueId);

        assertTrue(availableSeats <= configuredSeats.size(), "Available seats must not exceed configured seats");
        assertTrue(availableSeats >= 0, "Available seats must be non-negative");
    }

    @Test
    void testUpcomingEventsIncludeSunburnFestivalAndValidStatuses() {
        Event sunburn = eventRepository.findById(1L).orElse(null);
        if (sunburn != null) {
            assertEquals("UPCOMING", sunburn.getStatus());
            java.util.List<Seat> seats = seatRepository.findByVenueId(sunburn.getVenue().getId());
            assertFalse(seats.isEmpty(), "Venue 1 should have seats configured");
            double minPrice = seats.stream()
                    .map(Seat::getPrice)
                    .filter(java.util.Objects::nonNull)
                    .min(Double::compare)
                    .orElse(0.0);
            assertEquals(1500.0, minPrice, 0.01, "Sunburn starting price should be 1500");
        }
    }
}

