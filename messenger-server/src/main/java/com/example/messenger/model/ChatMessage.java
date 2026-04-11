package com.example.messenger.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
public class ChatMessage {
    private String content;
    private String sender;
    private String roomId;
    private LocalDateTime timestamp;
}
