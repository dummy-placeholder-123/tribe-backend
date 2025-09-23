package com.tribe.backend.chat.service;

import com.tribe.backend.chat.domain.ChatMessage;
import com.tribe.backend.chat.domain.ChatParticipant;
import com.tribe.backend.chat.domain.ChatRoom;
import com.tribe.backend.chat.domain.ChatType;
import com.tribe.backend.chat.dto.ChatMessageRequest;
import com.tribe.backend.chat.dto.ChatMessageResponse;
import com.tribe.backend.chat.dto.ChatRoomCreateRequest;
import com.tribe.backend.chat.dto.ChatRoomResponse;
import com.tribe.backend.chat.repository.ChatMessageRepository;
import com.tribe.backend.chat.repository.ChatParticipantRepository;
import com.tribe.backend.chat.repository.ChatRoomRepository;
import com.tribe.backend.common.dto.PageResponse;
import com.tribe.backend.common.exception.BadRequestException;
import com.tribe.backend.common.exception.ForbiddenException;
import com.tribe.backend.common.exception.NotFoundException;
import com.tribe.backend.event.repository.EventRepository;
import com.tribe.backend.user.domain.UserAccount;
import com.tribe.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final int MAX_CHAT_PAGE_SIZE = 100;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository participantRepository,
                       ChatMessageRepository messageRepository, UserRepository userRepository,
                       EventRepository eventRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public ChatRoomResponse createRoom(UUID creatorId, ChatRoomCreateRequest request) {
        UserAccount creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        ChatRoom room = new ChatRoom();
        room.setType(request.type());
        room.setTopic(request.topic());
        room.setCreatedBy(creator);
        if (request.type() == ChatType.EVENT) {
            if (request.eventId() == null) {
                throw new BadRequestException("Event chat requires eventId");
            }
            eventRepository.findById(request.eventId())
                .orElseThrow(() -> new NotFoundException("Event not found"));
            room.setEventId(request.eventId());
        }
        if (request.type() == ChatType.CLUB && request.clubId() == null) {
            throw new BadRequestException("Club chat requires clubId");
        }
        room.setClubId(request.clubId());
        ChatRoom saved = chatRoomRepository.save(room);
        ChatParticipant participant = new ChatParticipant();
        participant.setChatRoom(saved);
        participant.setUser(creator);
        participantRepository.save(participant);
        return toRoomResponse(saved);
    }

    @Transactional
    public ChatMessageResponse sendMessage(UUID chatId, UUID senderId, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));
        UserAccount sender = userRepository.findById(senderId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        ensureParticipant(chatRoom, sender);
        return messageRepository.findByChatRoomAndClientMessageId(chatRoom, request.clientMessageId())
            .map(this::toMessageResponse)
            .orElseGet(() -> persistMessage(chatRoom, sender, request));
    }

    public PageResponse<ChatMessageResponse> getRecentMessages(UUID chatId, UUID userId, int limit, UUID cursor) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        ensureParticipant(chatRoom, user);
        Instant cursorSentAt = null;
        UUID cursorId = null;
        if (cursor != null) {
            ChatMessage cursorMessage = messageRepository.findById(cursor)
                .filter(message -> message.getChatRoom().getId().equals(chatRoom.getId()))
                .orElseThrow(() -> new BadRequestException("Invalid cursor"));
            cursorSentAt = cursorMessage.getSentAt();
            cursorId = cursorMessage.getId();
        }
        int pageSize = Math.min(limit, MAX_CHAT_PAGE_SIZE);
        List<ChatMessageResponse> messages = messageRepository
            .findRecentMessages(chatRoom, cursorSentAt, cursorId,
                PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "sentAt", "id")))
            .stream()
            .map(this::toMessageResponse)
            .toList();
        String nextCursor = messages.size() == pageSize ? messages.get(messages.size() - 1).id().toString() : null;
        return new PageResponse<>(messages, nextCursor, nextCursor != null);
    }

    @Transactional
    public void markAsRead(UUID chatId, UUID userId, UUID messageId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        ChatParticipant participant = ensureParticipant(chatRoom, user);
        participant.setLastReadMessageId(messageId);
        participantRepository.save(participant);
    }

    public List<ChatRoomResponse> listRoomsForUser(UUID userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return participantRepository.findByUser(user).stream()
            .map(ChatParticipant::getChatRoom)
            .distinct()
            .map(this::toRoomResponse)
            .toList();
    }

    private ChatParticipant ensureParticipant(ChatRoom chatRoom, UserAccount user) {
        return participantRepository.findByChatRoomAndUser(chatRoom, user)
            .orElseThrow(() -> new ForbiddenException("User not part of chat"));
    }

    private ChatMessageResponse persistMessage(ChatRoom chatRoom, UserAccount sender, ChatMessageRequest request) {
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(request.content());
        message.setSentAt(Instant.now());
        message.setClientMessageId(request.clientMessageId());
        return toMessageResponse(messageRepository.save(message));
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room) {
        return new ChatRoomResponse(
            room.getId(),
            room.getType(),
            room.getTopic(),
            room.getEventId(),
            room.getClubId(),
            room.isArchived(),
            room.getCreatedAt()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getChatRoom().getId(),
            message.getSender().getId(),
            message.isDeleted() ? "Message removed" : message.getContent(),
            message.getSentAt(),
            message.getClientMessageId(),
            message.getModerationStatus(),
            message.isDeleted()
        );
    }
}
