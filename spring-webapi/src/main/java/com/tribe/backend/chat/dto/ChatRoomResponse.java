package com.tribe.backend.chat.dto;

import com.tribe.backend.chat.domain.ChatType;
import java.time.Instant;
import java.util.UUID;

public record ChatRoomResponse(
    UUID id,
    ChatType type,
    String topic,
    UUID eventId,
    UUID clubId,
    boolean archived,
    Instant createdAt
) {
}
