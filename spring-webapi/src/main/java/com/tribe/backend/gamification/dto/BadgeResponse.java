package com.tribe.backend.gamification.dto;

import java.time.Instant;
import java.util.UUID;

public record BadgeResponse(
    UUID id,
    String code,
    String name,
    String description,
    String iconUrl,
    Instant earnedAt
) {
}
