package com.tribe.backend.club.dto;

import com.tribe.backend.club.domain.ClubRole;
import java.time.Instant;
import java.util.UUID;

public record ClubMembershipResponse(
    UUID membershipId,
    UUID clubId,
    UUID userId,
    ClubRole role,
    Instant joinedAt
) {
}
