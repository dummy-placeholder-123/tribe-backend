package com.tribe.backend.user.dto;

import com.tribe.backend.user.domain.PremiumTier;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String displayName,
    String bio,
    String city,
    String avatarUrl,
    Set<String> interests,
    PremiumTier premiumTier,
    Instant createdAt,
    Instant lastActiveAt
) {
}
