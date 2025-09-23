package com.tribe.backend.event.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tribe.backend.event.dto.EventCreateRequest;
import com.tribe.backend.event.dto.EventResponse;
import com.tribe.backend.rsvp.domain.RsvpStatus;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.domain.UserRole;
import com.tribe.backend.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserAccount organizer;

    @BeforeEach
    void setUp() {
        organizer = new UserAccount();
        organizer.setEmail("organizer@example.com");
        organizer.setDisplayName("Organizer");
        organizer.setPasswordHash(passwordEncoder.encode("Password123!"));
        organizer.setCity("Austin");
        organizer.getRoles().add(UserRole.ORGANIZER);
        userRepository.save(organizer);
    }

    @Test
    void rsvpAppliesCapacityRules() {
        EventCreateRequest request = new EventCreateRequest(
            "Launch Party",
            "Exciting new feature",
            Instant.now().plus(1, ChronoUnit.DAYS),
            Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
            "HQ",
            30.0,
            -97.0,
            "Austin",
            1,
            false,
            Set.of("tech"),
            null
        );

        EventResponse event = eventService.createEvent(organizer.getId(), request);

        UserAccount attendeeOne = createUser("member1@example.com");
        UserAccount attendeeTwo = createUser("member2@example.com");

        RsvpStatus first = eventService.rsvpForEvent(attendeeOne.getId(), event.id());
        RsvpStatus second = eventService.rsvpForEvent(attendeeTwo.getId(), event.id());

        assertThat(first).isEqualTo(RsvpStatus.GOING);
        assertThat(second).isEqualTo(RsvpStatus.WAITLISTED);
    }

    private UserAccount createUser(String email) {
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setDisplayName(email);
        account.setCity("Austin");
        account.setPasswordHash(passwordEncoder.encode("Password123!"));
        account.getRoles().add(UserRole.USER);
        return userRepository.save(account);
    }
}
