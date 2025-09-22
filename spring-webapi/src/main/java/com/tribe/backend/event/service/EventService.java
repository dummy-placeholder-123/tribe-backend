package com.tribe.backend.event.service;

import com.tribe.backend.club.domain.Club;
import com.tribe.backend.club.repository.ClubRepository;
import com.tribe.backend.common.exception.BadRequestException;
import com.tribe.backend.common.exception.ForbiddenException;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.event.domain.Event;
import com.tribe.backend.event.dto.EventCreateRequest;
import com.tribe.backend.event.dto.EventResponse;
import com.tribe.backend.event.repository.EventRepository;
import com.tribe.backend.rsvp.domain.Rsvp;
import com.tribe.backend.rsvp.domain.RsvpStatus;
import com.tribe.backend.rsvp.service.RsvpService;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.domain.UserRole;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RsvpService rsvpService;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        ClubRepository clubRepository, RsvpService rsvpService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.rsvpService = rsvpService;
    }

    @Transactional
    public EventResponse createEvent(UUID creatorId, EventCreateRequest request) {
        if (request.startTime().isAfter(request.endTime())) {
            throw new BadRequestException("Event end time must be after start time");
        }
        UserAccount creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        if (creator.getRoles().stream().noneMatch(role -> role == UserRole.ORGANIZER || role == UserRole.ADMIN)) {
            throw new ForbiddenException("Only organizers can create events");
        }
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocationName(request.locationName());
        event.setLocationLat(request.locationLat());
        event.setLocationLng(request.locationLng());
        event.setCity(request.city());
        event.setCapacity(request.capacity());
        event.setPremiumOnly(request.premiumOnly());
        event.setCreatedBy(creator);
        if (request.clubId() != null) {
            Club club = clubRepository.findById(request.clubId())
                .orElseThrow(() -> new NotFoundException("Club not found"));
            event.setClub(club);
        }
        if (request.tags() != null) {
            event.setTags(request.tags().stream().map(String::toLowerCase).collect(Collectors.toSet()));
        }
        return toResponse(eventRepository.save(event));
    }

    public EventResponse getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    public List<EventResponse> listUpcoming(String city) {
        return eventRepository.findUpcomingByCity(city, Instant.now()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public RsvpStatus rsvpForEvent(UUID userId, UUID eventId) {
        Rsvp rsvp = rsvpService.rsvp(userId, eventId);
        return rsvp.getStatus();
    }

    @Transactional
    public void cancelRsvp(UUID userId, UUID eventId) {
        rsvpService.cancel(userId, eventId);
    }

    private EventResponse toResponse(Event event) {
        Set<String> tags = event.getTags() == null ? Set.of() : Set.copyOf(event.getTags());
        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartTime(),
            event.getEndTime(),
            event.getLocationName(),
            event.getLocationLat(),
            event.getLocationLng(),
            event.getCity(),
            event.getCapacity(),
            event.isPremiumOnly(),
            event.getVisibilityBoostUntil(),
            tags,
            event.getClub() != null ? event.getClub().getId() : null,
            event.getCreatedBy().getId()
        );
    }
}
