package com.tribe.backend.event.dto;

import com.tribe.backend.rsvp.domain.RsvpStatus;
import java.util.UUID;

public record EventRsvpResponse(
    UUID eventId,
    UUID userId,
    RsvpStatus status
) {
}
