package com.example.messenger.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.messenger.entity.ConversationEntity;
import com.example.messenger.entity.UserEntity;
import com.example.messenger.enums.MessageDeliveryStatus;
import com.example.messenger.enums.MessageType;
import com.example.messenger.mapper.ChatMessageMapper;
import com.example.messenger.model.ChatMessage;
import com.example.messenger.model.UnseenMessageCountResponse;
import com.example.messenger.model.UserConnection;
import com.example.messenger.model.Users;
import com.example.messenger.repository.ConversationRepository;
import com.example.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.example.messenger.utils.ConversationUtil.getConversationId;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ConversationRepository conversationRepository;
    private final OnlineOfflineService onlineOfflineService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    public List<UserConnection> getUserFriends(Authentication authentication) {
        Users userDetails = (Users) authentication.getPrincipal();
        String username = userDetails.getUsername();
        List<UserEntity> users = userRepository.findAll();
        UserEntity currentUser =
                users.stream()
                        .filter(user -> user.getUsername().equals(username))
                        .findFirst()
                        .orElseThrow(null);

        return users.stream()
                .filter(user -> !user.getUsername().equals(username))
                .map(
                        user ->
                                UserConnection.builder()
                                        .connectionId(user.getId())
                                        .connectionUsername(user.getUsername())
                                        .convId(getConversationId(user, currentUser))
                                        .unSeen(0)
                                        .isOnline(onlineOfflineService.isUserOnline(user.getId()))
                                        .build())
                .toList();
    }

    public List<UnseenMessageCountResponse> getUnseenMessageCount(Authentication authentication) {
        List<UnseenMessageCountResponse> result = new ArrayList<>();
        Users userDetails = (Users) authentication.getPrincipal();
        List<ConversationEntity> unseenMessages =
                conversationRepository.findUnseenMessagesCount(userDetails.getId());

        if (!CollectionUtils.isEmpty(unseenMessages)) {
            Map<UUID, List<ConversationEntity>> unseenMessageCountByUser = new HashMap<>();
            for (ConversationEntity entity : unseenMessages) {
                List<ConversationEntity> values =
                        unseenMessageCountByUser.getOrDefault(entity.getFromUser(), new ArrayList<>());
                values.add(entity);
                unseenMessageCountByUser.put(entity.getFromUser(), values);
            }
            log.info("there are some unseen messages for {}", userDetails.getUsername());
            unseenMessageCountByUser.forEach(
                    (user, entities) -> {
                        result.add(
                                UnseenMessageCountResponse.builder()
                                        .count((long) entities.size())
                                        .fromUser(user)
                                        .build());
                        updateMessageDelivery(user, entities, MessageDeliveryStatus.DELIVERED);
                    });
        }
        return result;
    }

    public List<ChatMessage> getUnseenMessages(UUID fromUserId, Authentication authentication) {
        List<ChatMessage> result = new ArrayList<>();
        Users userDetails = (Users) authentication.getPrincipal();
        List<ConversationEntity> unseenMessages =
                conversationRepository.findUnseenMessages(userDetails.getId(), fromUserId);

        if (!CollectionUtils.isEmpty(unseenMessages)) {
            log.info("there are some unseen messages for {} from {}", userDetails.getUsername(), fromUserId);
            updateMessageDelivery(fromUserId, unseenMessages, MessageDeliveryStatus.SEEN);
            result = chatMessageMapper.toChatMessages(unseenMessages, userDetails, MessageType.UNSEEN);
        }
        return result;
    }

    private void updateMessageDelivery(
            UUID user,
            List<ConversationEntity> entities,
            MessageDeliveryStatus messageDeliveryStatus) {
        entities.forEach(e -> e.setDeliveryStatus(messageDeliveryStatus.toString()));
        onlineOfflineService.notifySender(user, entities, messageDeliveryStatus);
        conversationRepository.saveAll(entities);
    }

    public List<ChatMessage> setReadMessages(List<ChatMessage> chatMessages, Authentication authentication) {
        Users userDetails = (Users) authentication.getPrincipal();
        List<UUID> inTransitMessageIds = chatMessages.stream().map(ChatMessage::getId).toList();
        List<ConversationEntity> conversationEntities =
                conversationRepository.findAllById(inTransitMessageIds);
        conversationEntities.forEach(
                message -> message.setDeliveryStatus(MessageDeliveryStatus.SEEN.toString()));
        List<ConversationEntity> saved = conversationRepository.saveAll(conversationEntities);

        return chatMessageMapper.toChatMessages(saved, userDetails, MessageType.CHAT);
    }
}
