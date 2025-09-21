package com.example.demo.user.service;

import com.example.demo.user.domain.UserProfile;
import com.example.demo.user.dto.UserProfileCreateRequest;
import com.example.demo.user.dto.UserProfileResponse;
import com.example.demo.user.dto.UserProfileUpdateRequest;
import com.example.demo.user.exception.InvalidUserInputException;
import com.example.demo.user.exception.UserConflictException;
import com.example.demo.user.exception.UserNotFoundException;
import com.example.demo.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfileResponse create(UserProfileCreateRequest request) {
        String username = requireValue(trimToNull(request.username()), "username");
        String email = requireValue(trimToNull(request.email()), "email");
        String displayName = requireValue(trimToNull(request.displayName()), "displayName");
        String bio = trimToNull(request.bio());
        String avatarUrl = trimToNull(request.avatarUrl());
        String location = trimToNull(request.location());
        String city = trimToNull(request.city());
        String country = trimToNull(request.country());
        String occupation = trimToNull(request.occupation());
        Collection<String> hobbies = sanitizeHobbies(request.hobbies());

        assertUniqueUsername(username, null);
        assertUniqueEmail(email, null);

        UserProfile profile = new UserProfile(
                username,
                email,
                displayName,
                bio,
                avatarUrl,
                location,
                city,
                country,
                occupation
        );
        profile.replaceHobbies(hobbies);

        UserProfile saved = repository.save(profile);
        synchronizeFriends(saved, request.friendIds());

        return UserProfileResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> findAll() {
        return repository.findAllWithFriends()
                .stream()
                .map(UserProfileResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findById(UUID id) {
        UserProfile profile = repository.findWithFriendsById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(id)));
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findByUsername(String username) {
        UserProfile profile = repository.findByUsernameIgnoreCase(username)
                .flatMap(existing -> repository.findWithFriendsById(existing.getId()))
                .orElseThrow(() -> new UserNotFoundException("User with username %s was not found".formatted(username)));
        return UserProfileResponse.fromEntity(profile);
    }

    public UserProfileResponse update(UUID id, UserProfileUpdateRequest request) {
        UserProfile profile = repository.findWithFriendsById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(id)));

        if (request.displayName() != null) {
            String displayName = requireValue(trimToNull(request.displayName()), "displayName");
            profile.setDisplayName(displayName);
        }

        if (request.bio() != null) {
            profile.setBio(trimToNull(request.bio()));
        }

        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(trimToNull(request.avatarUrl()));
        }

        if (request.location() != null) {
            profile.setLocation(trimToNull(request.location()));
        }

        if (request.city() != null) {
            profile.setCity(trimToNull(request.city()));
        }

        if (request.country() != null) {
            profile.setCountry(trimToNull(request.country()));
        }

        if (request.occupation() != null) {
            profile.setOccupation(trimToNull(request.occupation()));
        }

        if (request.hobbies() != null) {
            profile.replaceHobbies(sanitizeHobbies(request.hobbies()));
        }

        if (request.email() != null) {
            String email = requireValue(trimToNull(request.email()), "email");
            if (!email.equalsIgnoreCase(profile.getEmail())) {
                assertUniqueEmail(email, profile.getId());
                profile.setEmail(email);
            }
        }

        synchronizeFriends(profile, request.friendIds());

        return UserProfileResponse.fromEntity(profile);
    }

    public void delete(UUID id) {
        UserProfile profile = repository.findWithFriendsById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(id)));
        profile.synchronizeFriends(Set.of());
        repository.delete(profile);
    }

    private void assertUniqueUsername(String username, UUID currentUserId) {
        repository.findByUsernameIgnoreCase(username)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new UserConflictException("Username %s is already taken".formatted(username));
                });
    }

    private void assertUniqueEmail(String email, UUID currentUserId) {
        repository.findByEmailIgnoreCase(email)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new UserConflictException("Email %s is already in use".formatted(email));
                });
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String requireValue(String value, String fieldName) {
        if (value == null) {
            throw new InvalidUserInputException(fieldName + " cannot be blank");
        }
        return value;
    }

    private Collection<String> sanitizeHobbies(List<String> hobbies) {
        if (hobbies == null) {
            return List.of();
        }
        return hobbies.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void synchronizeFriends(UserProfile profile, Set<UUID> friendIds) {
        if (friendIds == null) {
            return;
        }
        Set<UserProfile> desired = resolveFriends(friendIds, profile.getId());
        profile.synchronizeFriends(desired);
        repository.save(profile);
    }

    private Set<UserProfile> resolveFriends(Set<UUID> friendIds, UUID currentUserId) {
        LinkedHashSet<UserProfile> friends = new LinkedHashSet<>();
        for (UUID friendId : friendIds) {
            if (friendId == null) {
                continue;
            }
            if (friendId.equals(currentUserId)) {
                throw new InvalidUserInputException("Users cannot be friends with themselves");
            }
            UserProfile friend = repository.findWithFriendsById(friendId)
                    .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(friendId)));
            friends.add(friend);
        }
        return friends;
    }
}
