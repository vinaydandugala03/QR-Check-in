-- Flyway baseline migration: V1__initial_schema.sql
-- Exact Gatepass database schema matching production tables, columns, indexes, and foreign keys.

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `venues` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `capacity` int DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `artist` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `event_date` date DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `venue_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqdxygdernwwt74hdvix9u5nr3` (`venue_id`),
  CONSTRAINT `FKqdxygdernwwt74hdvix9u5nr3` FOREIGN KEY (`venue_id`) REFERENCES `venues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `seats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` double DEFAULT NULL,
  `seat_number` varchar(255) DEFAULT NULL,
  `seat_type` varchar(255) DEFAULT NULL,
  `section` varchar(255) DEFAULT NULL,
  `venue_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfahhtb0bb0u1wcnqqys0iehmo` (`venue_id`),
  CONSTRAINT `FKfahhtb0bb0u1wcnqqys0iehmo` FOREIGN KEY (`venue_id`) REFERENCES `venues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `event_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2ww82bk3npaiyu9oeehwtt2q3` (`event_id`),
  KEY `FKeyog2oic85xg7hsu2je2lx3s6` (`user_id`),
  CONSTRAINT `FK2ww82bk3npaiyu9oeehwtt2q3` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
  CONSTRAINT `FKeyog2oic85xg7hsu2je2lx3s6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `booking_seats` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint DEFAULT NULL,
  `seat_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmbi9ciapn0nvat63t0a8tv478` (`booking_id`),
  KEY `FKm2vak166qv8osqwe5qcxsn1p` (`seat_id`),
  CONSTRAINT `FKm2vak166qv8osqwe5qcxsn1p` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`),
  CONSTRAINT `FKmbi9ciapn0nvat63t0a8tv478` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

