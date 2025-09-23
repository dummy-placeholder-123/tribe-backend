package com.tribe.backend.gamification.dto;

import com.tribe.backend.gamification.domain.StreakType;
import java.time.Instant;

public record StreakResponse(
    StreakType type,
    int currentCount,
    int longestCount,
    Instant lastIncrementedAt
) {
}
