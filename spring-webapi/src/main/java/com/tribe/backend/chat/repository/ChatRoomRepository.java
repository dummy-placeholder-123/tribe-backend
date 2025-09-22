package com.tribe.backend.chat.repository;

import com.tribe.backend.chat.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByEventId(UUID eventId);

    Optional<ChatRoom> findByClubId(UUID clubId);

    List<ChatRoom> findByCreatedById(UUID userId);
}
