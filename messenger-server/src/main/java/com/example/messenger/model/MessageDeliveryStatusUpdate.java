package com.example.messenger.model;
import java.util.UUID;

import com.example.messenger.enums.MessageDeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDeliveryStatusUpdate {
    private UUID id;
    private String content;
    private MessageDeliveryStatus messageDeliveryStatusEnum;
}
