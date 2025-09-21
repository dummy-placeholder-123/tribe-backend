package com.example.demo.user.dto;

import com.example.demo.user.domain.UserProfile;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        String location,
        String city,
        String country,
        String occupation,
        List<String> hobbies,
        List<UserFriendResponse> friends,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserProfileResponse fromEntity(UserProfile profile) {
        List<String> hobbies = List.copyOf(profile.getHobbies());
        List<UserFriendResponse> friends = profile.getFriends().stream()
                .sorted(Comparator.comparing(UserProfile::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(friend -> new UserFriendResponse(friend.getId(), friend.getUsername(), friend.getDisplayName()))
                .toList();

        return new UserProfileResponse(
                profile.getId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getLocation(),
                profile.getCity(),
                profile.getCountry(),
                profile.getOccupation(),
                hobbies,
                friends,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
