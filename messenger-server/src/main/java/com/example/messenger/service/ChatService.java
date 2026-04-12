package com.example.messenger.service;

import java.util.UUID;

import com.example.messenger.entity.ConversationEntity;
import com.example.messenger.enums.MessageDeliveryStatus;
import com.example.messenger.model.ChatMessage;
import com.example.messenger.model.Users;
import com.example.messenger.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final ConversationRepository conversationRepository;
    private final OnlineOfflineService onlineOfflineService;

    public void sendMessageToConvId(
            ChatMessage chatMessage, String conversationId, SimpMessageHeaderAccessor headerAccessor) {
        Users userDetails = getUser();
        UUID fromUserId = userDetails.getId();
        UUID toUserId = chatMessage.getReceiverId();
        populateContext(chatMessage, userDetails);
        boolean isTargetOnline = onlineOfflineService.isUserOnline(toUserId);
        boolean isTargetSubscribed =
                onlineOfflineService.isUserSubscribed(toUserId, "/topic/" + conversationId);
        chatMessage.setId(UUID.randomUUID());

        ConversationEntity.ConversationEntityBuilder conversationEntityBuilder =
                ConversationEntity.builder();

        conversationEntityBuilder
                .id(chatMessage.getId())
                .fromUser(fromUserId)
                .toUser(toUserId)
                .content(chatMessage.getContent())
                .convId(conversationId);
        if (!isTargetOnline) {
            log.info(
                    "{} is not online. Content saved in unseen messages", chatMessage.getReceiverUsername());
            conversationEntityBuilder.deliveryStatus(MessageDeliveryStatus.NOT_DELIVERED.toString());
            chatMessage.setMessageDeliveryStatus(MessageDeliveryStatus.NOT_DELIVERED);

        } else if (!isTargetSubscribed) {
            log.info(
                    "{} is online but not subscribed. sending to their private subscription",
                    chatMessage.getReceiverUsername());
            conversationEntityBuilder.deliveryStatus(MessageDeliveryStatus.DELIVERED.toString());
            chatMessage.setMessageDeliveryStatus(MessageDeliveryStatus.DELIVERED);
            simpMessageSendingOperations.convertAndSend("/topic/" + toUserId.toString(), chatMessage);

        } else {
            conversationEntityBuilder.deliveryStatus(MessageDeliveryStatus.SEEN.toString());
            chatMessage.setMessageDeliveryStatus(MessageDeliveryStatus.SEEN);
        }
        conversationRepository.save(conversationEntityBuilder.build());
        simpMessageSendingOperations.convertAndSend("/topic/" + conversationId, chatMessage);
    }

    private void populateContext(ChatMessage chatMessage, Users userDetails) {
        chatMessage.setSenderUsername(userDetails.getUsername());
        chatMessage.setSenderId(userDetails.getId());
    }

    public Users getUser() {
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (Users) object;
    }
}
