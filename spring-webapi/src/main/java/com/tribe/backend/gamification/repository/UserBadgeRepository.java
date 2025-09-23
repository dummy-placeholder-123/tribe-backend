package com.tribe.backend.gamification.repository;

import com.tribe.backend.gamification.domain.UserBadge;
import com.tribe.backend.user.domain.UserAccount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {

    List<UserBadge> findByUser(UserAccount user);
}
