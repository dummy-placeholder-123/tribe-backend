package com.tribe.backend.premium.dto;

import com.tribe.backend.user.domain.PremiumTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
    @NotNull PremiumTier tier,
    @NotBlank String paymentMethodId
) {
}
