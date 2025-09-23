package com.tribe.backend.club.dto;

import java.util.Set;
import java.util.UUID;

public record ClubResponse(
    UUID id,
    String name,
    String description,
    String city,
    boolean premiumOnly,
    Set<String> tags,
    UUID ownerId
) {
}
