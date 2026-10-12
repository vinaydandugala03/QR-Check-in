# EVENTTICKETINGSYSTEM — Complete REST API Documentation

Base URL: `http://localhost:4040`

All protected endpoints require the following HTTP request header:
```http
Authorization: Bearer <JWT_TOKEN>
```

All JSON error responses adhere to the standard `ErrorResponse` schema:
```json
{
  "timestamp": "2026-10-07T10:15:30.123456",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

---

## Table of Contents
1. [Authentication Endpoints](#1-authentication-endpoints)
2. [User Management Endpoints](#2-user-management-endpoints)
3. [Event Endpoints](#3-event-endpoints)
4. [Venue Endpoints](#4-venue-endpoints)
5. [Seat Endpoints](#5-seat-endpoints)
6. [Booking Endpoints](#6-booking-endpoints)
7. [Booking-Seat Endpoints](#7-booking-seat-endpoints)

---

## 1. Authentication Endpoints

### 1.1 User Login
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Authentication**: None (Public)
- **Role**: Anyone
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "mypassword123"
  }
  ```
- **Validation Rules**:
  - `email`: Required, valid email format.
  - `password`: Required.
- **Success Response (`200 OK`)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "Jane Doe",
      "email": "user@example.com",
      "role": "USER"
    }
  }
  ```
- **Error Responses**:
  - `401 Unauthorized`: Invalid email or incorrect password.
  - `400 Bad Request`: Missing email or password fields.

---

## 2. User Management Endpoints

### 2.1 Register User
- **Method**: `POST`
- **URL**: `/api/users`
- **Authentication**: None (Public)
- **Role**: Anyone
- **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "securepassword123"
  }
  ```
- **Validation & Business Rules**:
  - `name`: Required (non-blank).
  - `email`: Required, valid email format, must be unique across all users.
  - `password`: Minimum 6 characters. Encrypted with BCrypt before storage.
  - Role is **strictly forced** to `"USER"` by backend logic (any client-supplied `"role": "ADMIN"` is ignored).
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 2,
    "name": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER"
  }
  ```
- **Error Responses**:
  - `409 Conflict`: Email already registered.
  - `400 Bad Request`: Validation failure (short password, missing name, or invalid email).

### 2.2 List All Users
- **Method**: `GET`
- **URL**: `/api/users`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "name": "Admin User",
      "email": "admin@demo.com",
      "role": "ADMIN"
    },
    {
      "id": 2,
      "name": "Jane Doe",
      "email": "jane@example.com",
      "role": "USER"
    }
  ]
  ```
- **Error Responses**:
  - `401 Unauthorized`: Missing or invalid JWT.
  - `403 Forbidden`: Authenticated as regular `USER`.

### 2.3 Get User by ID
- **Method**: `GET`
- **URL**: `/api/users/{id}`
- **Authentication**: Required
- **Role**: Profile Owner or `ADMIN`
- **Path Parameters**: `id` (Long) — Target user ID.
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 2,
    "name": "Jane Doe",
    "email": "jane@example.com",
    "role": "USER"
  }
  ```
- **Error Responses**:
  - `403 Forbidden`: Authenticated user is attempting to access another user's profile.
  - `404 Not Found`: User ID does not exist.

### 2.4 Update User Profile
- **Method**: `PUT`
- **URL**: `/api/users/{id}`
- **Authentication**: Required
- **Role**: Profile Owner or `ADMIN`
- **Path Parameters**: `id` (Long) — Target user ID.
- **Request Body**:
  ```json
  {
    "name": "Jane Smith",
    "email": "janesmith@example.com"
  }
  ```
- **Business Rules**: Updates `name` and `email` only. Does **not** modify `password` or `role`.
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "janesmith@example.com",
    "role": "USER"
  }
  ```
- **Error Responses**:
  - `403 Forbidden`: Attempting to update another user's profile.
  - `409 Conflict`: Updated email is already registered by another user.
  - `404 Not Found`: User ID does not exist.

### 2.5 Change Password
- **Method**: `PUT`
- **URL**: `/api/users/{id}/password`
- **Authentication**: Required
- **Role**: Account Owner or `ADMIN`
- **Path Parameters**: `id` (Long) — Target user ID.
- **Request Body**:
  ```json
  {
    "currentPassword": "securepassword123",
    "newPassword": "newpassword456"
  }
  ```
- **Business Rules**:
  - Validates `currentPassword` against stored BCrypt hash.
  - Enforces `newPassword` minimum 6 characters.
  - Hashes `newPassword` with BCrypt.
- **Success Response (`200 OK`)**:
  ```
  "Password changed successfully"
  ```
- **Error Responses**:
  - `401 Unauthorized`: Current password does not match.
  - `400 Bad Request`: New password is shorter than 6 characters.
  - `403 Forbidden`: Attempting to change another user's password.

### 2.6 Delete User
- **Method**: `DELETE`
- **URL**: `/api/users/{id}`
- **Authentication**: Required
- **Role**: Account Owner or `ADMIN`
- **Path Parameters**: `id` (Long) — Target user ID.
- **Business Safeguard**: Rejects deletion if user has booking records (`bookingRepository.existsByUserId`).
- **Success Response (`200 OK`)**:
  ```
  "User deleted successfully"
  ```
- **Error Responses**:
  - `409 Conflict`: Cannot delete a user with booking history.
  - `403 Forbidden`: Insufficient permissions.
  - `404 Not Found`: User ID does not exist.

---

## 3. Event Endpoints

### 3.1 List All Events
- **Method**: `GET`
- **URL**: `/api/events`
- **Authentication**: None (Public)
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "name": "Sunburn Festival",
      "description": "Electronic dance music festival",
      "artist": "DJ Example",
      "category": "Music",
      "eventDate": "2026-12-20",
      "startTime": "18:00:00",
      "endTime": "23:00:00",
      "status": "UPCOMING",
      "venue": {
        "id": 1,
        "name": "Gachibowli Indoor Stadium",
        "location": "Gachibowli, Hyderabad",
        "capacity": 5500
      }
    }
  ]
  ```

### 3.2 Get Event by ID
- **Method**: `GET`
- **URL**: `/api/events/{id}`
- **Authentication**: None (Public)
- **Success Response (`200 OK`)**: Event entity object.
- **Error Response**: `404 Not Found` if event does not exist.

### 3.3 Create Event
- **Method**: `POST`
- **URL**: `/api/events`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Request Body**:
  ```json
  {
    "name": "Acoustic Night",
    "description": "Live acoustic performances",
    "artist": "Acoustic Band",
    "category": "Music",
    "eventDate": "2026-11-15",
    "startTime": "19:00:00",
    "endTime": "22:00:00",
    "status": "UPCOMING",
    "venue": {
      "id": 1
    }
  }
  ```
- **Validation**:
  - `name`: Required.
  - `eventDate`: Required.
  - `venue`: Required, referenced venue ID must exist in database.
- **Success Response (`200 OK`)**: Created Event object.
- **Error Responses**:
  - `400 Bad Request`: Missing required fields or venue ID.
  - `404 Not Found`: Referenced venue ID does not exist.
  - `403 Forbidden`: Role is `USER`.

### 3.4 Update Event
- **Method**: `PUT`
- **URL**: `/api/events/{id}`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Request Body**: Same structure as Create Event.
- **Success Response (`200 OK`)**: Updated Event object.

### 3.5 Delete Event
- **Method**: `DELETE`
- **URL**: `/api/events/{id}`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Business Safeguard**: Rejects deletion if event has booking history (`bookingRepository.existsByEventId`).
- **Success Response (`200 OK`)**: `"Event deleted successfully"`.
- **Error Responses**:
  - `409 Conflict`: Cannot delete an event that has booking history.
  - `403 Forbidden`: Role is `USER`.

---

## 4. Venue Endpoints

### 4.1 List All Venues
- **Method**: `GET`
- **URL**: `/api/venues`
- **Authentication**: None (Public)
- **Success Response (`200 OK`)**: Array of venue objects.

### 4.2 Get Venue by ID
- **Method**: `GET`
- **URL**: `/api/venues/{id}`
- **Authentication**: None (Public)

### 4.3 Create Venue
- **Method**: `POST`
- **URL**: `/api/venues`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Request Body**:
  ```json
  {
    "name": "Hitex Exhibition Center",
    "location": "Madhapur, Hyderabad",
    "capacity": 8000
  }
  ```
- **Validation**: `name` (required), `location` (required), `capacity` (positive integer).
- **Success Response (`200 OK`)**: Created Venue object.

### 4.4 Update Venue
- **Method**: `PUT`
- **URL**: `/api/venues/{id}`
- **Authentication**: Required
- **Role**: `ADMIN`

### 4.5 Delete Venue
- **Method**: `DELETE`
- **URL**: `/api/venues/{id}`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Business Safeguard**: Rejects deletion if venue is assigned to existing events (`eventRepository.existsByVenueId`).
- **Success Response (`200 OK`)**: `"Venue deleted successfully"`.
- **Error Responses**:
  - `409 Conflict`: Cannot delete a venue that is assigned to events.
  - `403 Forbidden`: Role is `USER`.

---

## 5. Seat Endpoints

### 5.1 List All Seats
- **Method**: `GET`
- **URL**: `/api/seats`
- **Authentication**: None (Public)
- **Success Response (`200 OK`)**: Array of seat objects.

### 5.2 Get Seat by ID
- **Method**: `GET`
- **URL**: `/api/seats/{id}`
- **Authentication**: None (Public)

### 5.3 Create Seat
- **Method**: `POST`
- **URL**: `/api/seats`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Request Body**:
  ```json
  {
    "seatNumber": "B1",
    "section": "VIP",
    "seatType": "PREMIUM",
    "price": 2500.0,
    "venue": {
      "id": 1
    }
  }
  ```
- **Validation**:
  - `seatNumber`: Required.
  - `price`: Required, non-negative (`>= 0`).
  - `venue`: Required, referenced venue ID must exist.
- **Success Response (`200 OK`)**: Created Seat object.

### 5.4 Update Seat
- **Method**: `PUT`
- **URL**: `/api/seats/{id}`
- **Authentication**: Required
- **Role**: `ADMIN`

### 5.5 Delete Seat
- **Method**: `DELETE`
- **URL**: `/api/seats/{id}`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Business Safeguard**: Rejects deletion if seat is referenced in booking records (`bookingSeatRepository.existsBySeatId`).
- **Success Response (`200 OK`)**: `"Seat deleted successfully"`.
- **Error Responses**:
  - `409 Conflict`: Cannot delete a seat referenced by booking records.
  - `403 Forbidden`: Role is `USER`.

---

## 6. Booking Endpoints

### 6.1 Create Booking
- **Method**: `POST`
- **URL**: `/api/bookings`
- **Authentication**: Required
- **Role**: Authenticated User (`USER` or `ADMIN`)
- **Request Body**:
  ```json
  {
    "eventId": 1,
    "seatIds": [1, 2]
  }
  ```
  *(Note: Any client-supplied `userId` is ignored; the authenticated JWT user is strictly assigned as the booking owner).*
- **Business Rules & Validations**:
  1. Event must exist and have an assigned venue.
  2. All `seatIds` must exist in database.
  3. No duplicate seat IDs allowed in the request.
  4. All seats must belong to the event venue (`seat.venue.id == event.venue.id`).
  5. Every seat price is validated server-side.
  6. Server checks if any seat is already booked with status `CONFIRMED` for this event.
  7. Server calculates `totalAmount` automatically (sum of seat prices).
  8. Saves `Booking` with `status = "CONFIRMED"` and creates `BookingSeat` records.
- **Success Response (`201 Created`)**:
  ```json
  {
    "bookingId": 4,
    "userId": 2,
    "userName": "Jane Doe",
    "eventId": 1,
    "eventName": "Sunburn Festival",
    "bookingDate": "2026-10-07T10:13:30.150",
    "totalAmount": 3500.0,
    "status": "CONFIRMED",
    "seatIds": [1, 2],
    "seatNumbers": ["A1", "A2"]
  }
  ```
- **Error Responses**:
  - `409 Conflict`: One or more seats are already booked for this event.
  - `400 Bad Request`: Seats do not belong to the event venue, or missing fields.
  - `404 Not Found`: Event or seat IDs do not exist.
  - `401 Unauthorized`: Missing or invalid token.

### 6.2 List All Bookings
- **Method**: `GET`
- **URL**: `/api/bookings`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Success Response (`200 OK`)**: Array of `BookingResponse` objects across all users.
- **Error Responses**:
  - `403 Forbidden`: Regular `USER` access denied.

### 6.3 Get User Booking History
- **Method**: `GET`
- **URL**: `/api/bookings/user/{userId}`
- **Authentication**: Required
- **Role**: Target User (Self) or `ADMIN`
- **Path Parameters**: `userId` (Long) — User ID whose history is requested.
- **Ownership Defense**: If a regular `USER` passes a `userId` other than their own, access is rejected in the service layer.
- **Success Response (`200 OK`)**: Array of `BookingResponse` objects.
- **Error Responses**:
  - `403 Forbidden`: Attempting to access another user's bookings.
  - `404 Not Found`: User ID does not exist.

### 6.4 Check Seat Availability
- **Method**: `GET`
- **URL**: `/api/bookings/seat-check?seatId={seatId}&eventId={eventId}`
- **Authentication**: None (Public)
- **Query Parameters**:
  - `seatId`: Long
  - `eventId`: Long
- **Success Response (`200 OK`)**:
  - `true` if seat is currently `CONFIRMED` for this event.
  - `false` if seat is available.

### 6.5 Cancel Booking
- **Method**: `PUT`
- **URL**: `/api/bookings/{bookingId}/cancel`
- **Authentication**: Required
- **Role**: Booking Owner or `ADMIN`
- **Path Parameters**: `bookingId` (Long) — ID of booking to cancel.
- **Business Rules**:
  - Service verifies authenticated caller owns the booking (or is `ADMIN`).
  - Booking must currently be in `CONFIRMED` status.
  - Status is updated to `CANCELLED`.
  - Seat becomes immediately available for new bookings.
  - Booking record is **not deleted** to preserve transaction history.
- **Success Response (`200 OK`)**:
  ```json
  {
    "bookingId": 4,
    "userId": 2,
    "userName": "Jane Doe",
    "eventId": 1,
    "eventName": "Sunburn Festival",
    "bookingDate": "2026-10-07T10:13:30.150",
    "totalAmount": 3500.0,
    "status": "CANCELLED",
    "seatIds": [1, 2],
    "seatNumbers": ["A1", "A2"]
  }
  ```
- **Error Responses**:
  - `403 Forbidden`: Non-owner attempting to cancel booking.
  - `409 Conflict`: Booking is already cancelled.
  - `404 Not Found`: Booking ID does not exist.

---

## 7. Booking-Seat Endpoints

### 7.1 List All Booking-Seat Records
- **Method**: `GET`
- **URL**: `/api/booking-seats`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Success Response (`200 OK`)**: Array of raw `BookingSeat` associations.

### 7.2 Create Manual Booking-Seat Association
- **Method**: `POST`
- **URL**: `/api/booking-seats`
- **Authentication**: Required
- **Role**: `ADMIN`
- **Request Body**:
  ```json
  {
    "booking": { "id": 1 },
    "seat": { "id": 2 }
  }
  ```
- **Success Response (`200 OK`)**: Saved `BookingSeat` entity.


---

## 8. Event Summary (public)

### 8.1 Booked seat IDs
- `GET /api/events/{id}/booked-seats` — public. Returns `[1, 2, 7]`, the seat IDs with a CONFIRMED booking for this event.

### 8.2 Event summary
- `GET /api/events/{id}/summary` — public.
- Response: `{ eventId, eventName, eventDate, status, totalSeats, bookedSeats, availableSeats, ticketsIssued, checkedIn }`

---

## 9. QR Tickets and Check-in

Every confirmed booking creates one ticket per seat. Each ticket has a unique random `ticketCode`; the QR image encodes `ETS:<ticketCode>`. Cancelling a booking removes its tickets (unless one was already used).

### 9.1 My tickets
- `GET /api/tickets/user/{userId}` — owner or `ADMIN`.
- Response: array of `{ ticketId, ticketCode, qrImage (PNG data URI), bookingId, eventId, eventName, eventDate, startTime, venueName, seatNumber, attendeeName, checkedIn, checkedInAt }`
- `403` when reading another user's tickets.

### 9.2 Check in
- `POST /api/checkin` — `ADMIN` or `GATE_STAFF` only (`403` for others).
- Body: `{ "ticketCode": "ETS:3f2c...", "eventId": 1 }` (`eventId` optional; when sent, tickets for other events are refused). The `ETS:` prefix is optional.
- Always `200` with a result: `{ status, message, ticket }` where `status` is one of
  - `SUCCESS` — checked in; stored in `tickets.checked_in`, `checked_in_at`, `checked_in_by`
  - `ALREADY_CHECKED_IN` — duplicate scan, nothing changes
  - `INVALID` — unknown code
  - `WRONG_EVENT` — ticket belongs to another event
  - `NOT_VALID` — booking cancelled or event cancelled
- The ticket row is locked while it is checked, so two simultaneous scans cannot both succeed.

### 9.3 Dashboard
- `GET /api/dashboard` — `ADMIN` or `GATE_STAFF`.
- Response: `{ totalEvents, totalUsers, totalBookings, revenue, ticketsIssued, checkedIn, events: [ <event summary> ] }`
