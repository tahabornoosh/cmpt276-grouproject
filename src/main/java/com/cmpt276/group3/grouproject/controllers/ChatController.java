package com.cmpt276.group3.grouproject.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UserBlock;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.ContactResponse;
import com.cmpt276.group3.grouproject.util.MessageResponse;
import com.cmpt276.group3.grouproject.util.SendMessageRequest;

import jakarta.servlet.http.HttpSession;

@Controller
public class ChatController {

    private final Auth auth;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserBlockRepository userBlockRepository;

    public ChatController(
        Auth auth,
        UserService userService,
        ChatMessageService chatMessageService,
        SimpMessagingTemplate messagingTemplate,
        UserBlockRepository userBlockRepository
    ) {
        this.auth = auth;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.userBlockRepository = userBlockRepository;
    }

    @GetMapping("/chat")
    public String loadChat(
        @RequestParam(required = false) Long userId,
        HttpSession session,
        Model model
    ) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<ContactResponse> contacts =
            chatMessageService.getExistingConversations(currentUser);

        model.addAttribute("requestedUserId", userId);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("contacts", contacts);

        return "chat";
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
        SendMessageRequest request,
        Principal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException(
                "You must be logged in to send messages"
            );
        }

        User sender = getCurrentWebSocketUser(principal);

        try {
            ChatMessage savedMessage =
                chatMessageService.createMessage(
                    sender,
                    request.recipientId(),
                    request.content()
                );

            MessageResponse response = MessageResponse.from(savedMessage);

            String recipientId =
                String.valueOf(savedMessage.getRecipient().getId());
            String senderId =
                String.valueOf(savedMessage.getSender().getId());

            messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/messages",
                response
            );
            messagingTemplate.convertAndSendToUser(
                senderId,
                "/queue/messages",
                response
            );
        } catch (IllegalStateException exception) {
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/chat-errors",
                Map.of(
                    "code", "CHAT_BLOCKED",
                    "message", exception.getMessage()
                )
            );
        }
    }

    @GetMapping("/api/chat/messages")
    @ResponseBody
    public List<MessageResponse> getConversation(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);
        return chatMessageService.getConversation(currentUser, otherUserId);
    }

    @PostMapping("/api/chat/messages/read")
    @ResponseBody
    public void markConversationAsRead(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);
        chatMessageService.markConversationAsRead(currentUser, otherUserId);
    }

    @GetMapping("/api/chat/block-status")
    @ResponseBody
    public Map<String, Object> getBlockStatus(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);
        User otherUser = requireOtherUser(currentUser, otherUserId);
        return blockStatusFor(currentUser, otherUser);
    }

    @PostMapping("/api/chat/block")
    @ResponseBody
    public Map<String, Object> blockUser(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);
        User otherUser = requireOtherUser(currentUser, otherUserId);

        if (!userBlockRepository.existsByBlockerIdAndBlockedId(
            currentUser.getId(),
            otherUser.getId()
        )) {
            userBlockRepository.save(new UserBlock(currentUser, otherUser));
        }

        notifyBlockStatus(currentUser, otherUser);
        return blockStatusFor(currentUser, otherUser);
    }

    @PostMapping("/api/chat/unblock")
    @ResponseBody
    public Map<String, Object> unblockUser(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);
        User otherUser = requireOtherUser(currentUser, otherUserId);

        userBlockRepository
            .findByBlockerIdAndBlockedId(
                currentUser.getId(),
                otherUser.getId()
            )
            .ifPresent(userBlockRepository::delete);

        notifyBlockStatus(currentUser, otherUser);
        return blockStatusFor(currentUser, otherUser);
    }

    private Map<String, Object> blockStatusFor(
        User currentUser,
        User otherUser
    ) {
        boolean blockedByCurrentUser =
            userBlockRepository.existsByBlockerIdAndBlockedId(
                currentUser.getId(),
                otherUser.getId()
            );
        boolean blockedByOtherUser =
            userBlockRepository.existsByBlockerIdAndBlockedId(
                otherUser.getId(),
                currentUser.getId()
            );

        return Map.of(
            "otherUserId", otherUser.getId(),
            "blockedByCurrentUser", blockedByCurrentUser,
            "blockedByOtherUser", blockedByOtherUser,
            "communicationBlocked",
                blockedByCurrentUser || blockedByOtherUser
        );
    }

    private void notifyBlockStatus(User firstUser, User secondUser) {
        messagingTemplate.convertAndSendToUser(
            String.valueOf(firstUser.getId()),
            "/queue/block-status",
            blockStatusFor(firstUser, secondUser)
        );
        messagingTemplate.convertAndSendToUser(
            String.valueOf(secondUser.getId()),
            "/queue/block-status",
            blockStatusFor(secondUser, firstUser)
        );
    }

    private User requireLoggedInUser(HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You must be logged in"
            );
        }

        User currentUser = auth.getUser(session);
        if (currentUser == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "The logged-in user could not be found"
            );
        }

        return currentUser;
    }

    private User requireOtherUser(User currentUser, long otherUserId) {
        if (Objects.equals(currentUser.getId(), otherUserId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot block yourself"
            );
        }

        User otherUser = userService.findUserById(otherUserId);
        if (otherUser == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The other user was not found"
            );
        }

        return otherUser;
    }

    private User getCurrentWebSocketUser(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        Long userId;

        try {
            userId = Long.parseLong(principal.getName());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                "WebSocket user identity is not a user Id"
            );
        }

        User user = userService.findUserById(userId);

        if (user == null) {
            throw new IllegalStateException("User does not exist");
        }

        return user;
    }
}
