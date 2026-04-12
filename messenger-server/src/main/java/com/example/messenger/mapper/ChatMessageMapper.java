package com.example.messenger.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.messenger.entity.ConversationEntity;
import com.example.messenger.entity.UserEntity;
import com.example.messenger.enums.MessageType;
import com.example.messenger.model.ChatMessage;
import com.example.messenger.model.Users;
import com.example.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageMapper {

    private final UserRepository userRepository;

    public List<ChatMessage> toChatMessages(
            List<ConversationEntity> conversationEntities,
            Users userDetails,
            MessageType messageType) {
        List<UUID> fromUsersIds =
                conversationEntities.stream().map(ConversationEntity::getFromUser).toList();
        Map<UUID, String> fromUserIdsToUsername =
                userRepository.findAllById(fromUsersIds).stream()
                        .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));

        return conversationEntities.stream()
                .map(e -> toChatMessage(e, userDetails, fromUserIdsToUsername, messageType))
                .toList();
    }

    private static ChatMessage toChatMessage(
            ConversationEntity e,
            Users userDetails,
            Map<UUID, String> fromUserIdsToUsername,
            MessageType messageType) {
        return ChatMessage.builder()
                .id(e.getId())
                .messageType(messageType)
                .content(e.getContent())
                .receiverId(e.getToUser())
                .receiverUsername(userDetails.getUsername())
                .senderId(e.getFromUser())
                .senderUsername(fromUserIdsToUsername.get(e.getFromUser()))
                .build();
    }
}
