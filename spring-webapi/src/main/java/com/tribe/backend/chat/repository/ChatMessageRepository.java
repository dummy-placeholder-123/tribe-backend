package com.tribe.backend.chat.repository;

import com.tribe.backend.chat.domain.ChatMessage;
import com.tribe.backend.chat.domain.ChatRoom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND (:cursor IS NULL OR m.id < :cursor) "
        + "ORDER BY m.sentAt DESC")
    List<ChatMessage> findRecentMessages(@Param("chatRoom") ChatRoom chatRoom,
                                         @Param("cursor") UUID cursor,
                                         Pageable pageable);

    Optional<ChatMessage> findByChatRoomAndClientMessageId(ChatRoom chatRoom, String clientMessageId);
}
