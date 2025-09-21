package com.example.demo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserProfileCreateRequest(
        @NotBlank(message = "username is required")
        @Size(max = 32, message = "username must be at most 32 characters")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 320, message = "email must be at most 320 characters")
        String email,

        @NotBlank(message = "displayName is required")
        @Size(max = 80, message = "displayName must be at most 80 characters")
        String displayName,

        @Size(max = 512, message = "bio must be at most 512 characters")
        String bio,

        @Size(max = 512, message = "avatarUrl must be at most 512 characters")
        String avatarUrl,

        @Size(max = 255, message = "location must be at most 255 characters")
        String location,

        @Size(max = 120, message = "city must be at most 120 characters")
        String city,

        @Size(max = 120, message = "country must be at most 120 characters")
        String country,

        @Size(max = 120, message = "occupation must be at most 120 characters")
        String occupation,

        @Size(max = 20, message = "hobbies must contain at most 20 values")
        List<@Size(max = 64, message = "each hobby must be at most 64 characters") String> hobbies,

        @Size(max = 100, message = "friendIds must contain at most 100 entries")
        Set<UUID> friendIds
) {
}
