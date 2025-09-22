package com.tribe.backend.chat.dto;

import com.tribe.backend.chat.domain.ModerationStatus;
import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(
    UUID id,
    UUID chatId,
    UUID senderId,
    String content,
    Instant sentAt,
    String clientMessageId,
    ModerationStatus moderationStatus,
    boolean deleted
) {
}
