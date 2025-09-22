package com.tribe.backend.chat.dto;

import com.tribe.backend.chat.domain.ChatType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChatRoomCreateRequest(
    @NotNull ChatType type,
    String topic,
    UUID eventId,
    UUID clubId
) {
}
