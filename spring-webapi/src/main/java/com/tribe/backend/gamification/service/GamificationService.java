package com.tribe.backend.gamification.service;

import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.gamification.domain.Badge;
import com.tribe.backend.gamification.domain.Streak;
import com.tribe.backend.gamification.domain.StreakType;
import com.tribe.backend.gamification.domain.UserBadge;
import com.tribe.backend.gamification.dto.BadgeResponse;
import com.tribe.backend.gamification.dto.LeaderboardEntry;
import com.tribe.backend.gamification.dto.StreakResponse;
import com.tribe.backend.gamification.repository.BadgeRepository;
import com.tribe.backend.gamification.repository.StreakRepository;
import com.tribe.backend.gamification.repository.UserBadgeRepository;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final StreakRepository streakRepository;

    public GamificationService(UserRepository userRepository, BadgeRepository badgeRepository,
                               UserBadgeRepository userBadgeRepository, StreakRepository streakRepository) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.streakRepository = streakRepository;
    }

    public List<BadgeResponse> badgesForUser(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return userBadgeRepository.findByUser(user).stream()
            .map(userBadge -> new BadgeResponse(
                userBadge.getBadge().getId(),
                userBadge.getBadge().getCode(),
                userBadge.getBadge().getName(),
                userBadge.getBadge().getDescription(),
                userBadge.getBadge().getIconUrl(),
                userBadge.getCreatedAt()
            ))
            .toList();
    }

    public List<StreakResponse> streaksForUser(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return streakRepository.findAll().stream()
            .filter(streak -> streak.getUser().equals(user))
            .map(streak -> new StreakResponse(
                streak.getType(),
                streak.getCurrentCount(),
                streak.getLongestCount(),
                streak.getLastIncrementedAt()
            ))
            .toList();
    }

    @Transactional
    public void recordInteraction(UUID userId, StreakType type) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        Streak streak = streakRepository.findByUserAndType(user, type)
            .orElseGet(() -> {
                Streak s = new Streak();
                s.setUser(user);
                s.setType(type);
                return s;
            });
        Instant now = Instant.now();
        if (streak.getLastIncrementedAt() == null
            || Duration.between(streak.getLastIncrementedAt(), now).toHours() >= 24) {
            if (streak.getLastIncrementedAt() != null && Duration.between(streak.getLastIncrementedAt(), now).toHours() > 48) {
                streak.setCurrentCount(0);
            }
            streak.setCurrentCount(streak.getCurrentCount() + 1);
            streak.setLongestCount(Math.max(streak.getLongestCount(), streak.getCurrentCount()));
            streak.setLastIncrementedAt(now);
        }
        streakRepository.save(streak);
    }

    public List<LeaderboardEntry> leaderboard(String city) {
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(1);
        return userRepository.findAll().stream()
            .filter(user -> city == null || city.equalsIgnoreCase(user.getCity()))
            .map(user -> new LeaderboardEntry(
                user.getId(),
                user.getDisplayName(),
                user.getInterests().size() + (user.getLastActiveAt() != null ? 1 : 0),
                0
            ))
            .sorted(Comparator.comparingInt(LeaderboardEntry::score).reversed())
            .map(entry -> new LeaderboardEntry(entry.userId(), entry.displayName(), entry.score(), rank.getAndIncrement()))
            .collect(Collectors.toList());
    }
}
