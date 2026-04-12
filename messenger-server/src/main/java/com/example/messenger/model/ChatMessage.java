package com.example.messenger.model;

import java.util.List;
import java.util.UUID;

import com.example.messenger.enums.MessageDeliveryStatus;
import com.example.messenger.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private UUID id;

    private String content;
    private MessageType messageType;

    private UUID senderId;
    private String senderUsername;

    private UUID receiverId;
    private String receiverUsername;

    private UserConnection userConnection;

    private MessageDeliveryStatus messageDeliveryStatus;

    private List<MessageDeliveryStatusUpdate> messageDeliveryStatusUpdates;
}
