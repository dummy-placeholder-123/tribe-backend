package com.tribe.backend.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record ClubCreateRequest(
    @NotBlank String name,
    @Size(max = 500) String description,
    @NotBlank String city,
    boolean premiumOnly,
    Set<String> tags
) {
}
