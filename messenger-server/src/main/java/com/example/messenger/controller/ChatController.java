package com.example.messenger.controller;

import com.example.messenger.model.ChatMessage;
import com.example.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/sendMessage/{convId}")
    public ChatMessage sendMessageToConvId(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            @DestinationVariable("convId") String conversationId) {
        chatService.sendMessageToConvId(chatMessage, conversationId, headerAccessor);
        return chatMessage;
    }
}
