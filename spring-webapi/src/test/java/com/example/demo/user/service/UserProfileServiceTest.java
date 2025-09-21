package com.example.demo.user.service;

import com.example.demo.user.dto.UserFriendResponse;
import com.example.demo.user.dto.UserProfileCreateRequest;
import com.example.demo.user.dto.UserProfileResponse;
import com.example.demo.user.dto.UserProfileUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserProfileService.class)
class UserProfileServiceTest {

    @Autowired
    private UserProfileService service;

    @Test
    void createUserProfile_withFriendsAndHobbies() {
        UserProfileResponse friend = service.create(new UserProfileCreateRequest(
                "friend1",
                "friend1@example.com",
                "Friend One",
                null,
                null,
                null,
                "Austin",
                "USA",
                "Engineer",
                List.of("Running"),
                Set.of()
        ));

        UserProfileResponse created = service.create(new UserProfileCreateRequest(
                "newuser",
                "user@example.com",
                "New User",
                "bio",
                "https://example.com/avatar.png",
                "Texas",
                "Austin",
                "USA",
                "Engineer",
                List.of("Hiking", "Gaming", "Hiking"),
                Set.of(friend.id())
        ));

        assertThat(created.hobbies()).containsExactly("Hiking", "Gaming");
        assertThat(created.friends()).hasSize(1);
        assertThat(created.friends().get(0).username()).isEqualTo("friend1");

        UserProfileResponse friendAfter = service.findById(friend.id());
        assertThat(friendAfter.friends())
                .extracting(UserFriendResponse::username)
                .containsExactly("newuser");
    }

    @Test
    void updateUserProfile_replacesFriendsAndAttributes() {
        UserProfileResponse alice = service.create(new UserProfileCreateRequest(
                "alice",
                "alice@example.com",
                "Alice",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of()
        ));
        UserProfileResponse bob = service.create(new UserProfileCreateRequest(
                "bob",
                "bob@example.com",
                "Bob",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of()
        ));
        UserProfileResponse carol = service.create(new UserProfileCreateRequest(
                "carol",
                "carol@example.com",
                "Carol",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of()
        ));

        UserProfileResponse updated = service.update(alice.id(), new UserProfileUpdateRequest(
                "Alice Smith",
                "Updated bio",
                null,
                null,
                "Texas",
                "Austin",
                "USA",
                "Designer",
                List.of("Cooking", "Travel"),
                Set.of(bob.id())
        ));

        assertThat(updated.displayName()).isEqualTo("Alice Smith");
        assertThat(updated.location()).isEqualTo("Texas");
        assertThat(updated.friends()).extracting(UserFriendResponse::username).containsExactly("bob");

        UserProfileResponse bobAfter = service.findById(bob.id());
        assertThat(bobAfter.friends()).extracting(UserFriendResponse::username).containsExactly("alice");

        UserProfileResponse updatedAgain = service.update(alice.id(), new UserProfileUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                "Dallas",
                null,
                null,
                List.of("Photography"),
                Set.of(carol.id())
        ));

        assertThat(updatedAgain.city()).isEqualTo("Dallas");
        assertThat(updatedAgain.friends()).extracting(UserFriendResponse::username).containsExactly("carol");
        assertThat(updatedAgain.hobbies()).containsExactly("Photography");

        UserProfileResponse bobAfterRemoval = service.findById(bob.id());
        assertThat(bobAfterRemoval.friends()).isEmpty();

        UserProfileResponse carolAfter = service.findById(carol.id());
        assertThat(carolAfter.friends()).extracting(UserFriendResponse::username).containsExactly("alice");
    }
}
