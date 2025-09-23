package com.tribe.backend.premium.repository;

import com.tribe.backend.premium.domain.PremiumSubscription;
import com.tribe.backend.premium.domain.SubscriptionStatus;
import com.tribe.backend.user.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumSubscriptionRepository extends JpaRepository<PremiumSubscription, UUID> {

    Optional<PremiumSubscription> findByUserAndStatus(UserAccount user, SubscriptionStatus status);
}
