-- QR tickets: one ticket per booked seat. Check-in state is stored here.
CREATE TABLE `tickets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ticket_code` varchar(64) NOT NULL,
  `booking_id` bigint NOT NULL,
  `seat_id` bigint NOT NULL,
  `event_id` bigint NOT NULL,
  `checked_in` bit(1) NOT NULL,
  `checked_in_at` datetime(6) DEFAULT NULL,
  `checked_in_by` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tickets_code` (`ticket_code`),
  KEY `idx_tickets_booking` (`booking_id`),
  KEY `idx_tickets_event` (`event_id`),
  CONSTRAINT `fk_tickets_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  CONSTRAINT `fk_tickets_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`),
  CONSTRAINT `fk_tickets_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Give tickets to bookings that already exist (keeps earlier data usable).
INSERT INTO `tickets` (`ticket_code`, `booking_id`, `seat_id`, `event_id`, `checked_in`, `created_at`)
SELECT UUID(), bs.booking_id, bs.seat_id, b.event_id, b'0', NOW(6)
FROM `booking_seats` bs
JOIN `bookings` b ON b.id = bs.booking_id
WHERE b.status = 'CONFIRMED' AND bs.seat_id IS NOT NULL AND b.event_id IS NOT NULL;
