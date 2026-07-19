package com.cmpt276.group3.grouproject.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.ChatMessageRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;
import com.cmpt276.group3.grouproject.util.ContactResponse;
import com.cmpt276.group3.grouproject.util.MessageResponse;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final UserBlockRepository userBlockRepository;

    public ChatMessageService(
        ChatMessageRepository chatMessageRepository,
        UserService userService,
        UserBlockRepository userBlockRepository
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.userBlockRepository = userBlockRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getExistingConversations(User currentUser) {
        requireCurrentUser(currentUser);

        long currentUserId = currentUser.getId();
        List<ChatMessage> messages =
            chatMessageRepository.findMessagesForUser(currentUserId);

        Map<Long, ChatMessage> latestMessageByContact = new HashMap<>();
        Map<Long, User> usersById = new HashMap<>();
        Set<Long> contactsWithUnreadMessages = new HashSet<>();

        for (ChatMessage message : messages) {
            boolean sentByCurrentUser =
                Objects.equals(message.getSender().getId(), currentUserId);

            User otherUser = sentByCurrentUser
                ? message.getRecipient()
                : message.getSender();

            long otherUserId = otherUser.getId();
            usersById.put(otherUserId, otherUser);

            ChatMessage currentLatest = latestMessageByContact.get(otherUserId);
            if (currentLatest == null
                || message.getSentAt().isAfter(currentLatest.getSentAt())) {
                latestMessageByContact.put(otherUserId, message);
            }

            boolean unreadForCurrentUser =
                Objects.equals(message.getRecipient().getId(), currentUserId)
                    && message.isUnread();

            if (unreadForCurrentUser) {
                contactsWithUnreadMessages.add(otherUserId);
            }
        }

        List<ContactResponse> contacts = new ArrayList<>();

        latestMessageByContact.entrySet().stream()
            .sorted(
                Map.Entry.<Long, ChatMessage>comparingByValue(
                    Comparator.comparing(ChatMessage::getSentAt)
                ).reversed()
            )
            .forEach(entry -> {
                long otherUserId = entry.getKey();
                ChatMessage latestMessage = entry.getValue();
                User otherUser = usersById.get(otherUserId);

                boolean latestSentByCurrentUser =
                    Objects.equals(
                        latestMessage.getSender().getId(),
                        currentUserId
                    );

                contacts.add(
                    new ContactResponse(
                        otherUser.getId(),
                        otherUser.getFirst_name(),
                        otherUser.getLast_name(),
                        latestMessage.getContent(),
                        latestMessage.getSentAt(),
                        latestSentByCurrentUser,
                        contactsWithUnreadMessages.contains(otherUserId)
                    )
                );
            });

        return contacts;
    }

    @Transactional
    public ChatMessage createMessage(
        User sender,
        Long recipientId,
        String content
    ) {
        if (sender == null) {
            throw new IllegalArgumentException("The sender is required.");
        }

        if (recipientId == null) {
            throw new IllegalArgumentException("The recipient is required.");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("The message cannot be empty");
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

        if (Objects.equals(sender.getId(), recipient.getId())) {
            throw new IllegalArgumentException("You cannot message yourself");
        }

        boolean blockedBySender =
            userBlockRepository.existsByBlockerIdAndBlockedId(
                sender.getId(),
                recipient.getId()
            );
        boolean blockedByRecipient =
            userBlockRepository.existsByBlockerIdAndBlockedId(
                recipient.getId(),
                sender.getId()
            );

        if (blockedBySender || blockedByRecipient) {
            throw new IllegalStateException(
                "Messaging is unavailable for this conversation."
            );
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(cleanedContent);
        message.setUnread(true);

        return chatMessageRepository.save(message);
    }

    @Transactional
    public List<MessageResponse> getConversation(
        User currentUser,
        long otherUserId
    ) {
        requireCurrentUser(currentUser);
        requireOtherUser(otherUserId);

        List<ChatMessage> messages =
            chatMessageRepository.findConversation(
                currentUser.getId(),
                otherUserId
            );

        markReceivedMessagesAsRead(messages, currentUser.getId(), otherUserId);

        return messages.stream()
            .map(MessageResponse::from)
            .toList();
    }

    @Transactional
    public void markConversationAsRead(User currentUser, long otherUserId) {
        requireCurrentUser(currentUser);
        requireOtherUser(otherUserId);

        List<ChatMessage> messages =
            chatMessageRepository.findConversation(
                currentUser.getId(),
                otherUserId
            );

        markReceivedMessagesAsRead(messages, currentUser.getId(), otherUserId);
    }

    private void markReceivedMessagesAsRead(
        List<ChatMessage> messages,
        long currentUserId,
        long otherUserId
    ) {
        for (ChatMessage message : messages) {
            boolean receivedByCurrentUser =
                Objects.equals(message.getRecipient().getId(), currentUserId)
                    && Objects.equals(message.getSender().getId(), otherUserId);

            if (receivedByCurrentUser && message.isUnread()) {
                message.setUnread(false);
                chatMessageRepository.save(message);
            }
        }
    }

    private void requireCurrentUser(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user is required");
        }
    }

    private User requireOtherUser(long otherUserId) {
        User otherUser = userService.findUserById(otherUserId);

        if (otherUser == null) {
            throw new IllegalArgumentException("The other user was not found");
        }

        return otherUser;
    }
}
