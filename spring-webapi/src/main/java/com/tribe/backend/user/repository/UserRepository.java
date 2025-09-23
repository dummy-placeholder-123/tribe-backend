package com.tribe.backend.user.repository;

import com.tribe.backend.user.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "interests"})
    Optional<UserAccount> findWithRolesByEmail(String email);

    @Query("SELECT u FROM UserAccount u WHERE u.city = :city AND u.id <> :userId")
    List<UserAccount> findPotentialMatches(@Param("userId") UUID userId, @Param("city") String city);

    @Query("SELECT u FROM UserAccount u JOIN u.interests i WHERE LOWER(i) IN :interests AND u.id <> :userId")
    List<UserAccount> findBySharedInterests(@Param("userId") UUID userId, @Param("interests") Set<String> interests);
}
