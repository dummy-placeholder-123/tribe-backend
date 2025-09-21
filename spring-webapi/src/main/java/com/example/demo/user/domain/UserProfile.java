package com.example.demo.user.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_profiles_username", columnNames = "username"),
                @UniqueConstraint(name = "uq_user_profiles_email", columnNames = "email")
        }
)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 32)
    private String username;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(length = 512)
    private String bio;

    @Column(length = 512)
    private String avatarUrl;

    @Column(length = 255)
    private String location;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String country;

    @Column(length = 120)
    private String occupation;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "user_hobbies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "hobby", length = 64, nullable = false)
    private Set<String> hobbies = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<UserProfile> friends = new LinkedHashSet<>();

    protected UserProfile() {
        // JPA only
    }

    public UserProfile(
            String username,
            String email,
            String displayName,
            String bio,
            String avatarUrl,
            String location,
            String city,
            String country,
            String occupation
    ) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.location = location;
        this.city = city;
        this.country = country;
        this.occupation = occupation;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getOccupation() {
        return occupation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<String> getHobbies() {
        return Collections.unmodifiableSet(hobbies);
    }

    public Set<UserProfile> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void replaceHobbies(Collection<String> hobbies) {
        this.hobbies.clear();
        if (hobbies == null) {
            return;
        }
        hobbies.stream()
                .filter(Objects::nonNull)
                .forEach(this.hobbies::add);
    }

    public void synchronizeFriends(Collection<UserProfile> desiredFriends) {
        Set<UserProfile> desired = desiredFriends == null
                ? Collections.emptySet()
                : new LinkedHashSet<>(desiredFriends);

        Set<UserProfile> current = new LinkedHashSet<>(friends);
        for (UserProfile friend : current) {
            if (!desired.contains(friend)) {
                removeFriend(friend);
            }
        }
        for (UserProfile friend : desired) {
            addFriend(friend);
        }
    }

    public void addFriend(UserProfile friend) {
        if (friend == null || friend == this) {
            return;
        }
        if (friends.add(friend)) {
            friend.friends.add(this);
        }
    }

    public void removeFriend(UserProfile friend) {
        if (friend == null) {
            return;
        }
        if (friends.remove(friend)) {
            friend.friends.remove(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserProfile that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
