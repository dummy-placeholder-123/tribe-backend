package com.example.demo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
        String avatarUrl
) {
}
