package com.tribe.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
    @NotBlank String content,
    @NotBlank String clientMessageId
) {
}
