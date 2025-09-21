package com.example.demo.user.repository;

import com.example.demo.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserProfile> findByUsernameIgnoreCase(String username);

    Optional<UserProfile> findByEmailIgnoreCase(String email);

    @Query("select distinct u from UserProfile u left join fetch u.friends where u.id = :id")
    Optional<UserProfile> findWithFriendsById(@Param("id") UUID id);

    @Query("select distinct u from UserProfile u left join fetch u.friends")
    List<UserProfile> findAllWithFriends();
}
