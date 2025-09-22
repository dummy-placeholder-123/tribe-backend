package com.tribe.backend.gamification.repository;

import com.tribe.backend.gamification.domain.Streak;
import com.tribe.backend.gamification.domain.StreakType;
import com.tribe.backend.user.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreakRepository extends JpaRepository<Streak, UUID> {

    Optional<Streak> findByUserAndType(UserAccount user, StreakType type);
}
