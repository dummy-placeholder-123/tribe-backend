package com.tribe.backend.event.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record EventResponse(
    UUID id,
    String title,
    String description,
    Instant startTime,
    Instant endTime,
    String locationName,
    Double locationLat,
    Double locationLng,
    String city,
    Integer capacity,
    boolean premiumOnly,
    Instant visibilityBoostUntil,
    Set<String> tags,
    UUID clubId,
    UUID createdBy
) {
}
