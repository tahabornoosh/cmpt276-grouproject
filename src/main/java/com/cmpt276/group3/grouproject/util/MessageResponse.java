package com.cmpt276.group3.grouproject.util;

import java.time.Instant;

import com.cmpt276.group3.grouproject.models.ChatMessage;

public record MessageResponse(
    long id,
    long senderId,
    long recipientId,
    String content,
    Instant sentAt
) {
    public static MessageResponse from (
        ChatMessage message
    ) {
        return new MessageResponse(
            message.getId(),
            message.getSender().getId(),
            message.getRecipient().getId(),
            message.getContent(),
            message.getSentAt()
        );
    }
}
