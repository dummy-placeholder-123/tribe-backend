package com.tribe.backend.user.service;

import com.tribe.backend.common.exception.ConflictException;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.user.domain.PremiumTier;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.domain.UserRole;
import com.tribe.backend.user.dto.UserProfileResponse;
import com.tribe.backend.user.dto.UserRecommendationResponse;
import com.tribe.backend.user.dto.UserRegistrationRequest;
import com.tribe.backend.user.dto.UserUpdateRequest;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfileResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ConflictException("Email already registered");
        }

        UserAccount user = new UserAccount();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setCity(request.city());
        user.setSignupSource(request.signupSource());
        user.setPremiumTier(PremiumTier.FREE);
        user.getRoles().add(UserRole.USER);
        if (request.interests() != null) {
            request.interests().forEach(user::addInterest);
        }
        user.touchLastActive();
        UserAccount saved = userRepository.save(user);
        return toProfile(saved);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UserUpdateRequest request) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.city() != null) {
            user.setCity(request.city());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.interests() != null) {
            user.getInterests().clear();
            request.interests().forEach(user::addInterest);
        }
        if (request.premiumTier() != null) {
            user.setPremiumTier(request.premiumTier());
        }
        user.touchLastActive();
        return toProfile(userRepository.save(user));
    }

    public UserProfileResponse getProfile(UUID userId) {
        return userRepository.findById(userId)
            .map(this::toProfile)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<UserRecommendationResponse> getRecommendations(UUID userId, int limit) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        Set<String> normalizedInterests = user.getInterests().stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        List<UserAccount> candidates = userRepository.findPotentialMatches(userId, user.getCity());
        if (!normalizedInterests.isEmpty()) {
            candidates.addAll(userRepository.findBySharedInterests(userId, normalizedInterests));
        }

        return candidates.stream()
            .distinct()
            .map(candidate -> toRecommendation(user, candidate))
            .sorted(Comparator.comparing(UserRecommendationResponse::compatibilityScore).reversed())
            .limit(limit)
            .toList();
    }

    private UserProfileResponse toProfile(UserAccount user) {
        return new UserProfileResponse(
            user.getId(),
            user.getDisplayName(),
            user.getBio(),
            user.getCity(),
            user.getAvatarUrl(),
            Set.copyOf(user.getInterests()),
            user.getPremiumTier(),
            user.getCreatedAt(),
            user.getLastActiveAt()
        );
    }

    private UserRecommendationResponse toRecommendation(UserAccount source, UserAccount candidate) {
        Set<String> mutualInterests = candidate.getInterests().stream()
            .map(String::toLowerCase)
            .filter(source.getInterests()::contains)
            .collect(Collectors.toSet());

        double baseScore = 0.5;
        if (source.getCity() != null && source.getCity().equalsIgnoreCase(candidate.getCity())) {
            baseScore += 0.2;
        }
        baseScore += Math.min(mutualInterests.size() * 0.1, 0.3);
        return new UserRecommendationResponse(
            candidate.getId(),
            candidate.getDisplayName(),
            mutualInterests,
            0,
            Math.min(baseScore, 0.99),
            "Ask about their favorite recent experience in " + source.getCity()
        );
    }

    @Transactional
    public void updateLastActive(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActiveAt(Instant.now());
            userRepository.save(user);
        });
    }
}
