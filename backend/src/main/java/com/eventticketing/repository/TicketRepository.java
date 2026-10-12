package com.eventticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventticketing.entity.Ticket;

import jakarta.persistence.LockModeType;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByBookingId(Long bookingId);

    List<Ticket> findByBookingUserIdOrderByCreatedAtDesc(Long userId);

    long countByEventId(Long eventId);

    long countByEventIdAndCheckedInTrue(Long eventId);

    long countByCheckedInTrue();

    /** Row lock so two scanners reading the same code at the same moment cannot both check it in. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.ticketCode = :code")
    Optional<Ticket> findByTicketCodeForUpdate(@Param("code") String code);
}
