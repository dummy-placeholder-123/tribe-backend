package com.tribe.backend.event.repository;

import com.tribe.backend.event.domain.Event;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e WHERE e.city = :city AND e.startTime >= :from ORDER BY e.startTime ASC")
    List<Event> findUpcomingByCity(@Param("city") String city, @Param("from") Instant from);
}
