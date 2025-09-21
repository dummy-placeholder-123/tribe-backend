package com.example.demo.user.dto;

import java.util.UUID;

public record UserFriendResponse(
        UUID id,
        String username,
        String displayName
) {
}
