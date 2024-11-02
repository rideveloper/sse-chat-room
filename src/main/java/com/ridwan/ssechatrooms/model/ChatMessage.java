package com.ridwan.ssechatrooms.model;

import java.time.LocalDateTime;

public record ChatMessage(
        String id,
        MessageType type,
        String content,
        String sender,
        String room,
        LocalDateTime timestamp
) {
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    public static ChatMessage createUserMessage(String content, String sender, String room) {
        return new ChatMessage(
                java.util.UUID.randomUUID().toString(),
                MessageType.CHAT,
                content,
                sender,
                room,
                LocalDateTime.now()
        );
    }

    public static ChatMessage createSystemMessage(MessageType type, String sender, String room, String content) {
        return new ChatMessage(
                java.util.UUID.randomUUID().toString(),
                type,
                content,
                sender,
                room,
                LocalDateTime.now()
        );
    }
}
