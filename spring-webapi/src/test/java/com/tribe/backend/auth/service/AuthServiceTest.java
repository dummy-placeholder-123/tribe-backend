package com.tribe.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tribe.backend.auth.dto.AuthResponse;
import com.tribe.backend.auth.dto.LoginRequest;
import com.tribe.backend.auth.dto.RefreshTokenRequest;
import com.tribe.backend.user.dto.UserRegistrationRequest;
import com.tribe.backend.user.service.UserService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    private static final String EMAIL = "auth@test.com";
    private static final String PASSWORD = "MySecurePass1!";

    @BeforeEach
    void setup() {
        try {
            userService.register(new UserRegistrationRequest(EMAIL, PASSWORD, "Auth Tester", "Austin", Set.of(), "web"));
        } catch (com.tribe.backend.common.exception.ConflictException ignored) {
            // user already exists
        }
    }

    @Test
    void loginAndRefreshReturnTokens() {
        AuthResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD));
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();

        AuthResponse refreshed = authService.refresh(new RefreshTokenRequest(response.refreshToken()));
        assertThat(refreshed.accessToken()).isNotEqualTo(response.accessToken());
    }
}
