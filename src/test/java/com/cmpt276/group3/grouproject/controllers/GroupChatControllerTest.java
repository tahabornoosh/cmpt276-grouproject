package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.GroupChatMessage;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.FriendGroupService;
import com.cmpt276.group3.grouproject.services.GroupChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.GroupMessageResponse;
import com.cmpt276.group3.grouproject.util.SendGroupMessageRequest;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class GroupChatControllerTest {

    @Mock
    private Auth auth;

    @Mock
    private UserService userService;

    @Mock
    private FriendGroupService friendGroupService;

    @Mock
    private GroupChatMessageService groupChatMessageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GroupChatController groupChatController;

    @Test
    void sendGroupMessage_broadcastsToEveryGroupMember() {
        Principal principal = () -> "1";
        SendGroupMessageRequest request =
            new SendGroupMessageRequest(10L, "Hello group");

        User sender = Mockito.mock(User.class);
        User firstMemberUser = Mockito.mock(User.class);
        User secondMemberUser = Mockito.mock(User.class);
        FriendGroup group = Mockito.mock(FriendGroup.class);
        GroupMembership firstMembership =
            Mockito.mock(GroupMembership.class);
        GroupMembership secondMembership =
            Mockito.mock(GroupMembership.class);

        when(userService.findUserById(1L)).thenReturn(sender);
        when(sender.getId()).thenReturn(1L);
        when(sender.getFirst_name()).thenReturn("Parsa");
        when(sender.getLast_name()).thenReturn("Student");
        when(group.getId()).thenReturn(10L);

        GroupChatMessage savedMessage =
            new GroupChatMessage(group, sender, "Hello group");
        savedMessage.setId(100L);

        when(
            groupChatMessageService.createMessage(
                sender,
                10L,
                "Hello group"
            )
        ).thenReturn(savedMessage);

        when(firstMembership.getUser()).thenReturn(firstMemberUser);
        when(secondMembership.getUser()).thenReturn(secondMemberUser);
        when(firstMemberUser.getId()).thenReturn(1L);
        when(secondMemberUser.getId()).thenReturn(2L);

        when(friendGroupService.getMemberships(group))
            .thenReturn(List.of(firstMembership, secondMembership));

        groupChatController.sendGroupMessage(request, principal);

        verify(groupChatMessageService)
            .createMessage(sender, 10L, "Hello group");

        verify(messagingTemplate).convertAndSendToUser(
            eq("1"),
            eq("/queue/group-messages"),
            Mockito.<GroupMessageResponse>argThat(response ->
                response.id().equals(100L)
                    && response.groupId().equals(10L)
                    && response.senderId() == 1L
                    && response.senderName().equals("Parsa Student")
                    && response.content().equals("Hello group")
            )
        );

        verify(messagingTemplate).convertAndSendToUser(
            eq("2"),
            eq("/queue/group-messages"),
            Mockito.<GroupMessageResponse>argThat(response ->
                response.id().equals(100L)
            )
        );
    }

    @Test
    void sendGroupMessage_sendsServiceErrorsToTheWebSocketUser() {
        Principal principal = () -> "1";
        SendGroupMessageRequest request =
            new SendGroupMessageRequest(10L, "Hello");

        User sender = Mockito.mock(User.class);

        when(userService.findUserById(1L)).thenReturn(sender);
        when(
            groupChatMessageService.createMessage(
                sender,
                10L,
                "Hello"
            )
        ).thenThrow(
            new IllegalStateException(
                "You must be a member of this group"
            )
        );

        groupChatController.sendGroupMessage(request, principal);

        verify(messagingTemplate).convertAndSendToUser(
            eq("1"),
            eq("/queue/group-chat-errors"),
            Mockito.<Map<String, String>>argThat(error ->
                error.get("code").equals("GROUP_CHAT_ERROR")
                    && error.get("message").equals(
                        "You must be a member of this group"
                    )
            )
        );
    }

    @Test
    void sendGroupMessage_rejectsMissingPrincipal() {
        SendGroupMessageRequest request =
            new SendGroupMessageRequest(10L, "Hello");

        assertThrows(
            IllegalArgumentException.class,
            () -> groupChatController.sendGroupMessage(
                request,
                null
            )
        );

        verifyNoInteractions(
            userService,
            friendGroupService,
            groupChatMessageService,
            messagingTemplate
        );
    }

    @Test
    void sendGroupMessage_rejectsInvalidWebSocketIdentity() {
        Principal principal = () -> "not-a-number";
        SendGroupMessageRequest request =
            new SendGroupMessageRequest(10L, "Hello");

        assertThrows(
            IllegalStateException.class,
            () -> groupChatController.sendGroupMessage(
                request,
                principal
            )
        );

        verifyNoInteractions(
            friendGroupService,
            groupChatMessageService,
            messagingTemplate
        );
    }

    @Test
    void getGroupMessages_returnsMessagesFromService() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);
        GroupMessageResponse response =
            Mockito.mock(GroupMessageResponse.class);
        List<GroupMessageResponse> expected = List.of(response);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(groupChatMessageService.getMessages(currentUser, 10L))
            .thenReturn(expected);

        List<GroupMessageResponse> actual =
            groupChatController.getGroupMessages(10L, session);

        assertSame(expected, actual);

        verify(groupChatMessageService)
            .getMessages(currentUser, 10L);
    }

    @Test
    void getGroupMessages_rejectsUnauthenticatedRequest() {
        HttpSession session = Mockito.mock(HttpSession.class);

        when(auth.isLoggedIn(session)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> groupChatController.getGroupMessages(
                10L,
                session
            )
        );

        assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
        );

        verifyNoInteractions(groupChatMessageService);
    }

    @Test
    void getGroupMessages_rejectsMissingSessionUser() {
        HttpSession session = Mockito.mock(HttpSession.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> groupChatController.getGroupMessages(
                10L,
                session
            )
        );

        assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
        );

        verifyNoInteractions(groupChatMessageService);
    }
}
