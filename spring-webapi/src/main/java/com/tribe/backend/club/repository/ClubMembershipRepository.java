package com.tribe.backend.club.repository;

import com.tribe.backend.club.domain.Club;
import com.tribe.backend.club.domain.ClubMembership;
import com.tribe.backend.club.domain.ClubRole;
import com.tribe.backend.user.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, UUID> {

    Optional<ClubMembership> findByClubAndUser(Club club, UserAccount user);

    List<ClubMembership> findByUser(UserAccount user);

    boolean existsByClubAndUserAndRole(Club club, UserAccount user, ClubRole role);
}
