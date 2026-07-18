package com.cmpt276.group3.grouproject.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.ChatUserResponse;
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
    private final UsersRepository usersRepository;

    public ChatController(
        Auth auth,
        UserService userService,
        ChatMessageService chatMessageService,
        SimpMessagingTemplate messagingTemplate,
        UsersRepository usersRepository
    ) {
        this.auth = auth;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/api/chat/users")
    @ResponseBody
    public List<ChatUserResponse> getAvailableChatUsers(HttpSession session) {
        User currentUser = requireLoggedInUser(session);

        Set<Long> existingConversationIds =
            chatMessageService.getExistingConversations(currentUser)
                .stream()
                .map(ContactResponse::userId)
                .collect(Collectors.toSet());

        return usersRepository.findAll()
            .stream()
            .filter(user ->
                !Objects.equals(user.getId(), currentUser.getId())
            )
            .filter(user ->
                !existingConversationIds.contains(user.getId())
            )
            .map(user ->
                new ChatUserResponse(
                    user.getId(),
                    user.getFirst_name(),
                    user.getLast_name()
                )
            )
            .toList();
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

        // The template opens this user only when the ID exists in contacts.
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
    }

    @GetMapping("/api/chat/messages")
    @ResponseBody
    public List<MessageResponse> getConversation(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);

        // Reading this endpoint means the user actually opened the chat.
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
