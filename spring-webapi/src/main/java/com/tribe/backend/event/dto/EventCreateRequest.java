package com.tribe.backend.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record EventCreateRequest(
    @NotBlank String title,
    @Size(max = 1000) String description,
    @NotNull @Future Instant startTime,
    @NotNull @Future Instant endTime,
    String locationName,
    Double locationLat,
    Double locationLng,
    @NotBlank String city,
    Integer capacity,
    boolean premiumOnly,
    Set<String> tags,
    UUID clubId
) {
}
