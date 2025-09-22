package com.tribe.backend.chat.repository;

import com.tribe.backend.chat.domain.ChatParticipant;
import com.tribe.backend.chat.domain.ChatRoom;
import com.tribe.backend.user.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, UserAccount user);

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    List<ChatParticipant> findByUser(UserAccount user);
}
