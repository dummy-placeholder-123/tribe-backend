package com.tribe.backend.club.service;

import com.tribe.backend.club.domain.Club;
import com.tribe.backend.club.domain.ClubMembership;
import com.tribe.backend.club.domain.ClubRole;
import com.tribe.backend.club.dto.ClubCreateRequest;
import com.tribe.backend.club.dto.ClubMembershipResponse;
import com.tribe.backend.club.dto.ClubResponse;
import com.tribe.backend.club.repository.ClubMembershipRepository;
import com.tribe.backend.club.repository.ClubRepository;
import com.tribe.backend.common.exception.BadRequestException;
import com.tribe.backend.common.exception.ConflictException;
import com.tribe.backend.common.exception.ForbiddenException;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.domain.UserRole;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public ClubService(ClubRepository clubRepository, ClubMembershipRepository membershipRepository,
                       UserRepository userRepository) {
        this.clubRepository = clubRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ClubResponse createClub(UUID ownerId, ClubCreateRequest request) {
        UserAccount owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new NotFoundException("Owner not found"));
        if (!owner.getRoles().contains(UserRole.ORGANIZER) && !owner.getRoles().contains(UserRole.ADMIN)) {
            throw new ForbiddenException("Only organizers can create clubs");
        }
        Club club = new Club();
        club.setName(request.name());
        club.setDescription(request.description());
        club.setCity(request.city());
        club.setOwner(owner);
        club.setPremiumOnly(request.premiumOnly());
        if (request.tags() != null) {
            club.setTags(request.tags().stream().map(String::toLowerCase).collect(Collectors.toSet()));
        }
        Club saved = clubRepository.save(club);
        ClubMembership membership = new ClubMembership();
        membership.setClub(saved);
        membership.setUser(owner);
        membership.setRole(ClubRole.ORGANIZER);
        membershipRepository.save(membership);
        return toResponse(saved);
    }

    @Transactional
    public ClubMembershipResponse joinClub(UUID userId, UUID clubId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        Club club = clubRepository.findById(clubId)
            .orElseThrow(() -> new NotFoundException("Club not found"));
        if (membershipRepository.findByClubAndUser(club, user).isPresent()) {
            throw new ConflictException("Already a member");
        }
        if (club.isPremiumOnly() && user.getRoles().stream().noneMatch(role -> role == UserRole.PREMIUM_PLUS || role == UserRole.ADMIN)) {
            throw new ForbiddenException("Premium tier required");
        }
        ClubMembership membership = new ClubMembership();
        membership.setClub(club);
        membership.setUser(user);
        membership.setRole(ClubRole.MEMBER);
        ClubMembership saved = membershipRepository.save(membership);
        return new ClubMembershipResponse(saved.getId(), club.getId(), user.getId(), saved.getRole(), saved.getCreatedAt());
    }

    public List<ClubResponse> listClubsByCity(String city) {
        return clubRepository.findByCityIgnoreCase(city).stream()
            .map(this::toResponse)
            .toList();
    }

    public List<ClubMembershipResponse> listMemberships(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return membershipRepository.findByUser(user).stream()
            .map(membership -> new ClubMembershipResponse(
                membership.getId(),
                membership.getClub().getId(),
                user.getId(),
                membership.getRole(),
                membership.getCreatedAt()
            ))
            .toList();
    }

    public ClubResponse getClub(UUID clubId) {
        return clubRepository.findById(clubId)
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("Club not found"));
    }

    private ClubResponse toResponse(Club club) {
        Set<String> tags = club.getTags() == null ? Set.of() : Set.copyOf(club.getTags());
        return new ClubResponse(
            club.getId(),
            club.getName(),
            club.getDescription(),
            club.getCity(),
            club.isPremiumOnly(),
            tags,
            club.getOwner().getId()
        );
    }
}
