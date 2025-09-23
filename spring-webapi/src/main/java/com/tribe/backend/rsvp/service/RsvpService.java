package com.tribe.backend.rsvp.service;

import com.tribe.backend.common.exception.ConflictException;
import com.tribe.backend.common.exception.ForbiddenException;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.event.domain.Event;
import com.tribe.backend.event.repository.EventRepository;
import com.tribe.backend.rsvp.domain.Rsvp;
import com.tribe.backend.rsvp.domain.RsvpStatus;
import com.tribe.backend.rsvp.repository.RsvpRepository;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.domain.UserRole;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RsvpService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RsvpRepository rsvpRepository;

    public RsvpService(EventRepository eventRepository, UserRepository userRepository,
                       RsvpRepository rsvpRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.rsvpRepository = rsvpRepository;
    }

    @Transactional
    public Rsvp rsvp(UUID userId, UUID eventId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getStartTime().isBefore(Instant.now())) {
            throw new ForbiddenException("Cannot RSVP to past events");
        }
        if (event.isPremiumOnly() && user.getRoles().stream().noneMatch(role -> role == UserRole.PREMIUM_PLUS || role == UserRole.ADMIN)) {
            throw new ForbiddenException("Premium tier required for this event");
        }
        return rsvpRepository.findByEventAndUser(event, user)
            .map(existing -> {
                if (existing.getStatus() == RsvpStatus.CANCELLED) {
                    existing.setStatus(resolveStatus(event));
                    return rsvpRepository.save(existing);
                }
                throw new ConflictException("Already RSVP'd");
            })
            .orElseGet(() -> {
                Rsvp rsvp = new Rsvp();
                rsvp.setEvent(event);
                rsvp.setUser(user);
                rsvp.setStatus(resolveStatus(event));
                return rsvpRepository.save(rsvp);
            });
    }

    @Transactional
    public void cancel(UUID userId, UUID eventId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));
        Rsvp rsvp = rsvpRepository.findByEventAndUser(event, user)
            .orElseThrow(() -> new NotFoundException("RSVP not found"));
        rsvp.setStatus(RsvpStatus.CANCELLED);
        rsvpRepository.save(rsvp);
    }

    private RsvpStatus resolveStatus(Event event) {
        if (event.getCapacity() == null) {
            return RsvpStatus.GOING;
        }
        long confirmed = rsvpRepository.countByEventAndStatus(event, RsvpStatus.GOING);
        return confirmed >= event.getCapacity() ? RsvpStatus.WAITLISTED : RsvpStatus.GOING;
    }
}
