package com.tribe.backend.user.dto;

import java.util.Set;
import java.util.UUID;

public record UserRecommendationResponse(
    UUID id,
    String displayName,
    Set<String> mutualInterests,
    int sharedClubs,
    double compatibilityScore,
    String icebreakerPrompt
) {
}
