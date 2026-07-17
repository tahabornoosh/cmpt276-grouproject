package com.cmpt276.group3.grouproject.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.ChatMessageRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.util.MessageResponse;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    public ChatMessageService(
        ChatMessageRepository chatMessageRepository,
        UserService userService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
    }

    public ChatMessage createMessage(
        User sender,
        Long recipientId,
        String content
    ) {
        if (sender == null) {
            throw new IllegalArgumentException(
                "The sender is required."
            );
        }

        if (recipientId == null) {
            throw new IllegalArgumentException(
                "The recipient is required."
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                "The message cannot be empty"
            );
        }

        String cleanedContent = content.trim();

        if (cleanedContent.length() > 300) {
            throw new IllegalArgumentException(
                "The message cannot exceed 300 characters"
            );
        }

        User recipient = userService.findUserById(recipientId);
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient not found");
        }

        if (sender.getId() == recipient.getId()) {
            throw new IllegalArgumentException(
                "You cannot message yourself"
            );
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(cleanedContent);
        message.setSentAt(Instant.now());

        return chatMessageRepository.save(message);
    }

    public List<MessageResponse> getConversation(
        User currentUser,
        long otherUserId
    ) {
        if (currentUser == null) {
            throw new IllegalArgumentException(
                "Current user is required"
            );
        }

        User otherUser = userService.findUserById(otherUserId);

        if (otherUser == null) {
            throw new IllegalArgumentException(
                "The other user was not found"
            );
        }

        return chatMessageRepository.findConversation(currentUser.getId(), otherUserId)
                    .stream()
                    .map(MessageResponse::from)
                    .toList();
    }
}
