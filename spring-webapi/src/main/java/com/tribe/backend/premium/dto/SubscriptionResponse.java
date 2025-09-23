package com.tribe.backend.premium.dto;

import com.tribe.backend.premium.domain.SubscriptionStatus;
import com.tribe.backend.user.domain.PremiumTier;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
    UUID subscriptionId,
    PremiumTier tier,
    SubscriptionStatus status,
    Instant startedAt,
    Instant expiresAt
) {
}
