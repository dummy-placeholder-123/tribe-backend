package com.tribe.backend.rsvp.repository;

import com.tribe.backend.event.domain.Event;
import com.tribe.backend.rsvp.domain.Rsvp;
import com.tribe.backend.rsvp.domain.RsvpStatus;
import com.tribe.backend.user.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RsvpRepository extends JpaRepository<Rsvp, UUID> {

    Optional<Rsvp> findByEventAndUser(Event event, UserAccount user);

    @Query("SELECT COUNT(r) FROM Rsvp r WHERE r.event = :event AND r.status = :status")
    long countByEventAndStatus(@Param("event") Event event, @Param("status") RsvpStatus status);
}
