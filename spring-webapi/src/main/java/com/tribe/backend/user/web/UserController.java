package com.tribe.backend.user.web;

import com.tribe.backend.common.dto.PageResponse;
import com.tribe.backend.user.dto.UserProfileResponse;
import com.tribe.backend.user.dto.UserRecommendationResponse;
import com.tribe.backend.user.dto.UserRegistrationRequest;
import com.tribe.backend.user.dto.UserUpdateRequest;
import com.tribe.backend.user.service.UserService;
import com.tribe.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.register(request);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR') or #userId == principal.id")
    public UserProfileResponse getProfile(@PathVariable UUID userId,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return userService.getProfile(userId);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("#userId == principal.id")
    public UserProfileResponse updateProfile(@PathVariable UUID userId,
                                             @Valid @RequestBody UserUpdateRequest request,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        return userService.updateProfile(userId, request);
    }

    @GetMapping("/{userId}/recommendations")
    @PreAuthorize("#userId == principal.id")
    public PageResponse<UserRecommendationResponse> recommendations(@PathVariable UUID userId,
                                                                    @RequestParam(defaultValue = "10") int limit,
                                                                    @AuthenticationPrincipal UserPrincipal principal) {
        List<UserRecommendationResponse> items = userService.getRecommendations(userId, limit);
        return new PageResponse<>(items, null, false);
    }
}
