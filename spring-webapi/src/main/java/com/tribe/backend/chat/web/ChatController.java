package com.tribe.backend.chat.web;

import com.tribe.backend.chat.dto.ChatMessageRequest;
import com.tribe.backend.chat.dto.ChatMessageResponse;
import com.tribe.backend.chat.dto.ChatRoomCreateRequest;
import com.tribe.backend.chat.dto.ChatRoomResponse;
import com.tribe.backend.chat.service.ChatService;
import com.tribe.backend.common.dto.PageResponse;
import com.tribe.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse createRoom(@AuthenticationPrincipal UserPrincipal principal,
                                       @Valid @RequestBody ChatRoomCreateRequest request) {
        return chatService.createRoom(principal.getId(), request);
    }

    @GetMapping
    public List<ChatRoomResponse> myChats(@AuthenticationPrincipal UserPrincipal principal) {
        return chatService.listRoomsForUser(principal.getId());
    }

    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse send(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable UUID chatId,
                                    @Valid @RequestBody ChatMessageRequest request) {
        return chatService.sendMessage(chatId, principal.getId(), request);
    }

    @GetMapping("/{chatId}/messages")
    public PageResponse<ChatMessageResponse> history(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable UUID chatId,
                                                     @RequestParam(defaultValue = "50") int limit,
                                                     @RequestParam(required = false) UUID cursor) {
        return chatService.getRecentMessages(chatId, principal.getId(), limit, cursor);
    }

    @PostMapping("/{chatId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal UserPrincipal principal,
                         @PathVariable UUID chatId,
                         @RequestParam UUID messageId) {
        chatService.markAsRead(chatId, principal.getId(), messageId);
    }
}
