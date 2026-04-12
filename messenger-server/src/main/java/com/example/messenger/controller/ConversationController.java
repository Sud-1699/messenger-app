package com.example.messenger.controller;

import com.example.messenger.model.ChatMessage;
import com.example.messenger.model.UnseenMessageCountResponse;
import com.example.messenger.model.UserConnection;
import com.example.messenger.service.ConversationService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping(("/api/conversation"))
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/friends")
    public List<UserConnection> getUserFriends(Authentication connectedUser) {
        return conversationService.getUserFriends(connectedUser);
    }

    @GetMapping("/unseenMessages")
    public List<UnseenMessageCountResponse> getUnseenMessageCount(Authentication connectedUser) {
        return conversationService.getUnseenMessageCount(connectedUser);
    }

    @GetMapping("/unseenMessages/{fromUserId}")
    public List<ChatMessage> getUnseenMessages(
            @PathVariable("fromUserId") UUID fromUserId,
            Authentication connectedUser
    ) {
        return conversationService.getUnseenMessages(fromUserId, connectedUser);
    }

    @PutMapping("/setReadMessages")
    public List<ChatMessage> setReadMessages(
            @RequestBody List<ChatMessage> chatMessages,
            Authentication connectedUser
    ) {
        return conversationService.setReadMessages(chatMessages, connectedUser);
    }
}
