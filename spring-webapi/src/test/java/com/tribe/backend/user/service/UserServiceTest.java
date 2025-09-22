package com.tribe.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tribe.backend.common.exception.ConflictException;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.dto.UserProfileResponse;
import com.tribe.backend.user.dto.UserRegistrationRequest;
import com.tribe.backend.user.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUserPersistsProfileAndHash() {
        UserRegistrationRequest request = new UserRegistrationRequest(
            "test@example.com", "Str0ngPass!", "Tester", "Austin", Set.of("music"), "ios");

        UserProfileResponse response = userService.register(request);

        assertThat(response.displayName()).isEqualTo("Tester");
        UserAccount persisted = userRepository.findById(response.id()).orElseThrow();
        assertThat(persisted.getPasswordHash()).isNotEqualTo("Str0ngPass!");
        assertThat(persisted.getRoles()).isNotEmpty();
    }

    @Test
    void registerDuplicateEmailThrowsConflict() {
        UserRegistrationRequest request = new UserRegistrationRequest(
            "conflict@example.com", "Password123!", "Person", "Austin", Set.of(), "web");
        userService.register(request);

        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(ConflictException.class);
    }
}
