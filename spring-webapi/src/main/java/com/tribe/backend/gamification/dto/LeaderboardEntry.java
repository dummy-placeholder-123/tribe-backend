package com.tribe.backend.gamification.dto;

import java.util.UUID;

public record LeaderboardEntry(
    UUID userId,
    String displayName,
    int score,
    int rank
) {
}
