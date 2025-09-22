package com.tribe.backend.gamification.domain;

import com.tribe.backend.common.domain.BaseEntity;
import com.tribe.backend.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_streaks")
public class Streak extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreakType type;

    @Column(name = "current_count", nullable = false)
    private int currentCount = 0;

    @Column(name = "longest_count", nullable = false)
    private int longestCount = 0;

    @Column(name = "last_incremented_at")
    private Instant lastIncrementedAt;

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public StreakType getType() {
        return type;
    }

    public void setType(StreakType type) {
        this.type = type;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public int getLongestCount() {
        return longestCount;
    }

    public void setLongestCount(int longestCount) {
        this.longestCount = longestCount;
    }

    public Instant getLastIncrementedAt() {
        return lastIncrementedAt;
    }

    public void setLastIncrementedAt(Instant lastIncrementedAt) {
        this.lastIncrementedAt = lastIncrementedAt;
    }
}
