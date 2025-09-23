package com.tribe.backend.event.web;

import com.tribe.backend.event.dto.EventCreateRequest;
import com.tribe.backend.event.dto.EventResponse;
import com.tribe.backend.event.dto.EventRsvpResponse;
import com.tribe.backend.event.service.EventService;
import com.tribe.backend.rsvp.domain.RsvpStatus;
import com.tribe.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public EventResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                @Valid @RequestBody EventCreateRequest request) {
        return eventService.createEvent(principal.getId(), request);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable UUID eventId) {
        return eventService.getEvent(eventId);
    }

    @GetMapping
    public List<EventResponse> listUpcoming(@RequestParam String city) {
        return eventService.listUpcoming(city);
    }

    @PostMapping("/{eventId}/rsvp")
    public EventRsvpResponse rsvp(@PathVariable UUID eventId,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        RsvpStatus status = eventService.rsvpForEvent(principal.getId(), eventId);
        return new EventRsvpResponse(eventId, principal.getId(), status);
    }

    @DeleteMapping("/{eventId}/rsvp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRsvp(@PathVariable UUID eventId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        eventService.cancelRsvp(principal.getId(), eventId);
    }
}
