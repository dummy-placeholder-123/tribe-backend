package com.tribe.backend.chat.repository;

import com.tribe.backend.chat.domain.ChatMessage;
import com.tribe.backend.chat.domain.ChatRoom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND "
        + "(:sentAt IS NULL OR m.sentAt < :sentAt OR (m.sentAt = :sentAt AND m.id < :cursorId)) "
        + "ORDER BY m.sentAt DESC, m.id DESC")
    List<ChatMessage> findRecentMessages(@Param("chatRoom") ChatRoom chatRoom,
                                         @Param("sentAt") Instant sentAt,
                                         @Param("cursorId") UUID cursorId,
                                         Pageable pageable);

    Optional<ChatMessage> findByChatRoomAndClientMessageId(ChatRoom chatRoom, String clientMessageId);
}
