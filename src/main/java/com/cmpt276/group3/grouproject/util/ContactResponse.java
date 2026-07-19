package com.cmpt276.group3.grouproject.util;

import java.time.Instant;

public record ContactResponse (
            long userId,
            String firstName,
            String lastName,
            String lastMessage,
            Instant lastMessageAt,
            boolean lastMessageSentByCurrentUser,
            boolean unread
) { }    
