package com.tribe.backend.gamification.repository;

import com.tribe.backend.gamification.domain.Badge;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {

    Optional<Badge> findByCode(String code);
}
