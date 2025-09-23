package com.tribe.backend.user.dto;

import com.tribe.backend.user.domain.PremiumTier;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserUpdateRequest(
    @Size(max = 250) String bio,
    String city,
    String avatarUrl,
    Set<String> interests,
    PremiumTier premiumTier
) {
}
