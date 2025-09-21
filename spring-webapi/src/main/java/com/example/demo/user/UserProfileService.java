package com.example.demo.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

        assertUniqueUsername(username, null);
        assertUniqueEmail(email, null);

        UserProfile profile = new UserProfile(username, email, displayName, bio, avatarUrl);
        UserProfile saved = repository.save(profile);
        return UserProfileResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(UserProfileResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findById(UUID id) {
        UserProfile profile = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(id)));
        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findByUsername(String username) {
        UserProfile profile = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException("User with username %s was not found".formatted(username)));
        return UserProfileResponse.fromEntity(profile);
    }

    public UserProfileResponse update(UUID id, UserProfileUpdateRequest request) {
        UserProfile profile = repository.findById(id)
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

        if (request.email() != null) {
            String email = requireValue(trimToNull(request.email()), "email");
            if (!email.equalsIgnoreCase(profile.getEmail())) {
                assertUniqueEmail(email, profile.getId());
                profile.setEmail(email);
            }
        }

        UserProfile saved = repository.save(profile);
        return UserProfileResponse.fromEntity(saved);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("User with id %s was not found".formatted(id));
        }
        repository.deleteById(id);
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
}
