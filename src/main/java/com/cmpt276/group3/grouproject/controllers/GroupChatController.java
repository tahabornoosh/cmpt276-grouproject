package com.cmpt276.group3.grouproject.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.models.GroupChatMessage;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.GroupMessageResponse;
import com.cmpt276.group3.grouproject.util.SendGroupMessageRequest;

import jakarta.servlet.http.HttpSession;

import com.cmpt276.group3.grouproject.services.FriendGroupService;
import com.cmpt276.group3.grouproject.services.GroupChatMessageService;

@Controller
public class GroupChatController {
    
    private final Auth auth;
    private final UserService userService;
    private final FriendGroupService friendGroupService;
    private final GroupChatMessageService groupChatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupChatController(
        Auth auth,
        UserService userService,
        FriendGroupService friendGroupService,
        GroupChatMessageService groupChatMessageService,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.auth = auth;
        this.userService = userService;
        this.friendGroupService = friendGroupService;
        this.groupChatMessageService = groupChatMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/group-chat.send")
    public void sendGroupMessage(
        SendGroupMessageRequest request,
        Principal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException(
                "You must be logged in to send group messages"
            );
        }
        
        User sender = getCurrentWebSocketUser(principal);

        try {
            if (request == null) {
                throw new IllegalArgumentException(
                    "The group message request is required"
                );
            }

            GroupChatMessage savedMessage = 
                groupChatMessageService.createMessage(
                    sender,
                    request.groupId(),
                    request.content()
                );
            
            GroupMessageResponse response = GroupMessageResponse.from(savedMessage);
            List<GroupMembership> memberships = 
                friendGroupService.getMemberships(savedMessage.getGroup());

            for (GroupMembership membership : memberships) {
                if (membership.getRole() == GroupRole.PENDING) {
                    continue;
                }

                String memberId = String.valueOf(membership.getUser().getId());

                messagingTemplate.convertAndSendToUser(memberId, "/queue/group-messages", response);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/group-chat-errors", Map.of(
                "code", "GROUP_CHAT_ERROR",
                "message", exception.getMessage()
            )
        );
        }
    }

    @GetMapping("/api/group-chat/messages")
    @ResponseBody
    public List<GroupMessageResponse> getGroupMessages(
        @RequestParam Long groupId,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);

        try {
            return groupChatMessageService.getMessages(currentUser, groupId);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
            );
        }
    }

    // Helpers

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
                "The logged in user could not be found"
            );
        }

        return currentUser;
    }

    private User getCurrentWebSocketUser(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException(
                "The WebSocket user is not authenticated."
            );
        }

        long userId;

        try {
            userId = Long.parseLong(principal.getName());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                "The WebSocket identity is not a valid user ID"
            );
        }

        User user = userService.findUserById(userId);

        if (user == null) {
            throw new IllegalStateException(
                "The WebSocket user does not exist"
            );
        }

        return user;
    }
}
