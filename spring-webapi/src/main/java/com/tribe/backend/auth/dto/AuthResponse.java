package com.tribe.backend.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {
}
