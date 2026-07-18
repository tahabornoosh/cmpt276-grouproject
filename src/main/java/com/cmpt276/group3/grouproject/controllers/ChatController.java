package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.models.ChatMessageRepository;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.MessageResponse;
import com.cmpt276.group3.grouproject.util.SendMessageRequest;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;


@Controller
public class ChatController {
    private final UsersRepository usersRepository;
    private final Auth auth;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(Auth auth, UserService userService, ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate, UsersRepository usersRepository) {
        this.auth = auth;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.usersRepository = usersRepository;
    }


     @GetMapping("/chat")
    public String loadChat(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        var contacts = usersRepository.findAll()
                .stream()
                .filter(user -> user.getId() != currentUser.getId())
                .toList();

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

        String recipientId = String.valueOf(savedMessage.getRecipient().getId());
        String senderId = String.valueOf(savedMessage.getSender().getId());

        messagingTemplate.convertAndSendToUser(recipientId, "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", response);
    }
    
    @GetMapping("/api/chat/messages")
    @ResponseBody
    public List<MessageResponse> getConversation(
        @RequestParam long otherUserId,
        HttpSession session
    ) {
        if (!auth.isLoggedIn(session)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "You must be logged in"
            );
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The logged-in user could not be found");
        }

        return chatMessageService.getConversation(currentUser, otherUserId);
    }

    // Helpers

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
