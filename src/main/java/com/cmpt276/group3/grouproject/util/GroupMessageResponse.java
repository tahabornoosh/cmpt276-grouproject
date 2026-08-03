package com.cmpt276.group3.grouproject.util;

import java.time.Instant;

import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.GroupChatMessage;
import com.cmpt276.group3.grouproject.models.User;

public record GroupMessageResponse (
    Long id,
    Long groupId,
    long senderId,
    String senderName,
    String content,
    Instant sentAt
) {
    public static GroupMessageResponse from(
        GroupChatMessage message
    ) {
        if (message == null) {
            throw new IllegalArgumentException(
                "The group message is required"
            );
        }

        FriendGroup group = message.getGroup();
        User sender = message.getSender();

        if (group == null) {
            throw new IllegalStateException(
                "The group message does not have a group."
            );
        }

        if (sender == null) {
            throw new IllegalStateException(
                "The group message does not have a sender."
            );
        }

        String senderName = buildSenderName(sender);

        return new GroupMessageResponse(
            message.getId(),
            group.getId(),
            sender.getId(),
            senderName,
            message.getContent(),
            message.getSentAt()
        );
    }

    private static String buildSenderName(User sender) {
        String firstName = sender.getFirst_name();
        String lastName = sender.getLast_name();

        String fullName = String.join(
            " ",
            firstName == null ? "" : firstName.trim(),
            lastName == null ? "" : lastName.trim()
        ).trim();

        if (fullName.isEmpty()) {
            return "Unknown user";
        }

        return fullName;
    }
}
