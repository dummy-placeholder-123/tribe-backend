package com.tribe.backend.premium.service;

import com.tribe.backend.common.exception.ConflictException;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.premium.domain.PremiumSubscription;
import com.tribe.backend.premium.domain.SubscriptionStatus;
import com.tribe.backend.premium.dto.SubscriptionRequest;
import com.tribe.backend.premium.dto.SubscriptionResponse;
import com.tribe.backend.premium.repository.PremiumSubscriptionRepository;
import com.tribe.backend.user.domain.PremiumTier;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.domain.UserRole;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PremiumService {

    private final PremiumSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public PremiumService(PremiumSubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SubscriptionResponse startSubscription(UUID userId, SubscriptionRequest request) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .ifPresent(subscription -> { throw new ConflictException("Subscription already active"); });
        PremiumSubscription subscription = new PremiumSubscription();
        subscription.setUser(user);
        subscription.setTier(request.tier());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        subscription.setExternalSubscriptionId("simulated_" + UUID.randomUUID());
        PremiumSubscription saved = subscriptionRepository.save(subscription);
        user.setPremiumTier(request.tier());
        if (request.tier() == PremiumTier.PLUS || request.tier() == PremiumTier.ELITE) {
            user.getRoles().add(UserRole.PREMIUM_PLUS);
        }
        userRepository.save(user);
        return toResponse(saved);
    }

    public SubscriptionResponse activeSubscription(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .map(this::toResponse)
            .orElse(null);
    }

    @Transactional
    public void cancelSubscription(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .ifPresent(subscription -> {
                subscription.setStatus(SubscriptionStatus.CANCELED);
                subscription.setExpiresAt(Instant.now());
                subscriptionRepository.save(subscription);
            });
        user.setPremiumTier(PremiumTier.FREE);
        user.getRoles().remove(UserRole.PREMIUM_PLUS);
        userRepository.save(user);
    }

    private SubscriptionResponse toResponse(PremiumSubscription subscription) {
        return new SubscriptionResponse(
            subscription.getId(),
            subscription.getTier(),
            subscription.getStatus(),
            subscription.getStartedAt(),
            subscription.getExpiresAt()
        );
    }
}
