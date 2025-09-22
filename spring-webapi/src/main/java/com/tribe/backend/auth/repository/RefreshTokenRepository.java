package com.tribe.backend.auth.repository;

import com.tribe.backend.auth.domain.RefreshToken;
import com.tribe.backend.user.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(UserAccount user);
}
