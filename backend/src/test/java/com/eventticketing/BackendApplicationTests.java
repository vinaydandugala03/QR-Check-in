package com.eventticketing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventticketing.security.JwtService;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.flywaydb.core.Flyway flyway;

    @Test
    void contextLoads() {
        assertNotNull(jwtService);
        assertNotNull(passwordEncoder);
        assertNotNull(flyway);
    }

    @Test
    void testFlywayBaselineMigrationStatus() {
        assertNotNull(flyway);
        org.flywaydb.core.api.MigrationInfoService info = flyway.info();
        assertNotNull(info);
        assertNotNull(info.current(), "Flyway baseline or current migration should exist");
        assertEquals("1", info.current().getVersion().getVersion(), "Current migration/baseline version should be 1");
    }

    @Test
    void testNormalizeJdbcUrl() {
        assertEquals(
                "jdbc:mysql://localhost:3306/event_ticketing_db",
                BackendApplication.normalizeJdbcUrl("mysql://localhost:3306/event_ticketing_db")
        );
        assertEquals(
                "jdbc:mysql://host:15595/defaultdb?sslMode=REQUIRED",
                BackendApplication.normalizeJdbcUrl("mysql://host:15595/defaultdb?ssl-mode=REQUIRED")
        );
        assertEquals(
                "jdbc:mysql://host:15595/defaultdb?sslMode=REQUIRED",
                BackendApplication.normalizeJdbcUrl("jdbc:mysql://host:15595/defaultdb?sslMode=REQUIRED")
        );
        assertEquals(null, BackendApplication.normalizeJdbcUrl(null));
        assertEquals("", BackendApplication.normalizeJdbcUrl("   "));
    }

    @Test
    void testJwtTokenGenerationAndClaims() {
        String email = "testunit@demo.com";
        String role = "USER";
        Long userId = 99L;

        String token = jwtService.generateToken(email, role, userId);
        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(role, jwtService.extractRole(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertTrue(jwtService.isTokenValid(token, email));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testBCryptPasswordHashing() {
        String rawPassword = "mySecurePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Autowired
    private com.eventticketing.service.SeatService seatService;

    @Autowired
    private com.eventticketing.repository.VenueRepository venueRepository;

    @Autowired
    private com.eventticketing.repository.SeatRepository seatRepository;

    @Test
    void testBulkSeatCreationAndConflictValidation() {
        // Create a temporary venue for testing bulk seats
        com.eventticketing.entity.Venue venue = new com.eventticketing.entity.Venue();
        venue.setName("Bulk Test Amphitheatre");
        venue.setLocation("Gachibowli, Hyderabad");
        venue.setCapacity(500);
        com.eventticketing.entity.Venue savedVenue = venueRepository.save(venue);

        try {
            // 1. Bulk create A1-B5 in PREMIUM tier at 750 INR
            com.eventticketing.dto.BulkSeatCreationRequest req1 = new com.eventticketing.dto.BulkSeatCreationRequest(
                    savedVenue.getId(),
                    "PREMIUM",
                    "A",
                    "B",
                    5,
                    750.0
            );

            java.util.List<com.eventticketing.entity.Seat> createdSeats = seatService.createSeatsBulk(req1);
            assertEquals(10, createdSeats.size()); // Rows A (5) + B (5) = 10

            // Verify prices and numbering
            for (com.eventticketing.entity.Seat s : createdSeats) {
                assertEquals(750.0, s.getPrice());
                assertEquals("PREMIUM", s.getSection());
                assertEquals(savedVenue.getId(), s.getVenue().getId());
                assertTrue(s.getSeatNumber().matches("^[AB][1-5]$"));
            }

            // 2. Conflict test: creating overlapping seats (e.g. A1-A3) should throw IllegalStateException
            com.eventticketing.dto.BulkSeatCreationRequest conflictReq = new com.eventticketing.dto.BulkSeatCreationRequest(
                    savedVenue.getId(),
                    "VIP",
                    "A",
                    "A",
                    3,
                    1500.0
            );

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> seatService.createSeatsBulk(conflictReq)
            );

            // 3. Validation test: invalid row sequence should throw IllegalArgumentException
            com.eventticketing.dto.BulkSeatCreationRequest invalidRowReq = new com.eventticketing.dto.BulkSeatCreationRequest(
                    savedVenue.getId(),
                    "VIP",
                    "Z",
                    "A",
                    5,
                    1500.0
            );

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> seatService.createSeatsBulk(invalidRowReq)
            );

            // 4. Non-overlapping bulk create (C1-C5 in VIP tier at 1500 INR) should succeed
            com.eventticketing.dto.BulkSeatCreationRequest req2 = new com.eventticketing.dto.BulkSeatCreationRequest(
                    savedVenue.getId(),
                    "VIP",
                    "C",
                    "C",
                    5,
                    1500.0
            );
            java.util.List<com.eventticketing.entity.Seat> vipSeats = seatService.createSeatsBulk(req2);
            assertEquals(5, vipSeats.size());
            assertEquals(1500.0, vipSeats.get(0).getPrice());
            assertEquals("VIP", vipSeats.get(0).getSection());

        } finally {
            // Clean up test data
            java.util.List<com.eventticketing.entity.Seat> allVenueSeats = seatRepository.findByVenueId(savedVenue.getId());
            seatRepository.deleteAll(allVenueSeats);
            venueRepository.delete(savedVenue);
        }
    }
}
