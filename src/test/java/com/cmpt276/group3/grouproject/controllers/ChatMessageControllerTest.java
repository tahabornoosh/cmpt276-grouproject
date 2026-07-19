package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UserBlock;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.MessageResponse;
import com.cmpt276.group3.grouproject.util.SendMessageRequest;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private Auth auth;

    @Mock
    private UserService userService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserBlockRepository userBlockRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private ChatController chatController;

    @Test
    void sendMessage_sendsResponseToSenderAndRecipient() {
        User sender = Mockito.mock(User.class);
        User recipient = Mockito.mock(User.class);
        ChatMessage savedMessage = Mockito.mock(ChatMessage.class);

        Principal principal = () -> "1";
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");
        Instant sentAt = Instant.parse("2026-07-18T03:10:00Z");

        when(userService.findUserById(1L)).thenReturn(sender);
        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);

        when(chatMessageService.createMessage(sender, 2L, "Hello"))
            .thenReturn(savedMessage);

        when(savedMessage.getId()).thenReturn(100L);
        when(savedMessage.getSender()).thenReturn(sender);
        when(savedMessage.getRecipient()).thenReturn(recipient);
        when(savedMessage.getContent()).thenReturn("Hello");
        when(savedMessage.getSentAt()).thenReturn(sentAt);

        chatController.sendMessage(request, principal);

        ArgumentCaptor<MessageResponse> responseCaptor =
            ArgumentCaptor.forClass(MessageResponse.class);

        verify(chatMessageService)
            .createMessage(sender, 2L, "Hello");

        verify(messagingTemplate)
            .convertAndSendToUser(
                eq("2"),
                eq("/queue/messages"),
                responseCaptor.capture()
            );

        MessageResponse response = responseCaptor.getValue();

        assertAll(
            () -> assertEquals(100L, response.id()),
            () -> assertEquals(1L, response.senderId()),
            () -> assertEquals(2L, response.recipientId()),
            () -> assertEquals("Hello", response.content()),
            () -> assertEquals(sentAt, response.sentAt())
        );

        verify(messagingTemplate)
            .convertAndSendToUser(
                "1",
                "/queue/messages",
                response
            );
    }

    @Test
    void sendMessage_withoutPrincipal_throwsException() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");

        assertThrows(
            IllegalArgumentException.class,
            () -> chatController.sendMessage(request, null)
        );

        verifyNoInteractions(
            userService,
            chatMessageService,
            messagingTemplate,
            userBlockRepository,
            usersRepository
        );
    }

    @Test
    void blockUser_savesBlock_returnsStatus_andNotifiesBothUsers() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);
        User otherUser = Mockito.mock(User.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(currentUser.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);
        when(userService.findUserById(2L)).thenReturn(otherUser);

        when(
            userBlockRepository.existsByBlockerIdAndBlockedId(1L, 2L)
        ).thenReturn(false, true, true, true);

        when(
            userBlockRepository.existsByBlockerIdAndBlockedId(2L, 1L)
        ).thenReturn(false);

        Map<String, Object> response =
            chatController.blockUser(2L, session);

        ArgumentCaptor<UserBlock> blockCaptor =
            ArgumentCaptor.forClass(UserBlock.class);

        verify(userBlockRepository).save(blockCaptor.capture());

        UserBlock savedBlock = blockCaptor.getValue();

        assertAll(
            () -> assertSame(currentUser, savedBlock.getBlocker()),
            () -> assertSame(otherUser, savedBlock.getBlocked()),
            () -> assertTrue(
                (Boolean) response.get("blockedByCurrentUser")
            ),
            () -> assertFalse(
                (Boolean) response.get("blockedByOtherUser")
            ),
            () -> assertTrue(
                (Boolean) response.get("communicationBlocked")
            )
        );

        verify(messagingTemplate)
            .convertAndSendToUser(
                eq("1"),
                eq("/queue/block-status"),
                Mockito.<Map<String, Object>>argThat(status ->
                    Long.valueOf(2L).equals(status.get("otherUserId"))
                        && Boolean.TRUE.equals(
                            status.get("blockedByCurrentUser")
                        )
                        && Boolean.TRUE.equals(
                            status.get("communicationBlocked")
                        )
                )
            );

        verify(messagingTemplate)
            .convertAndSendToUser(
                eq("2"),
                eq("/queue/block-status"),
                Mockito.<Map<String, Object>>argThat(status ->
                    Long.valueOf(1L).equals(status.get("otherUserId"))
                        && Boolean.TRUE.equals(
                            status.get("blockedByOtherUser")
                        )
                        && Boolean.TRUE.equals(
                            status.get("communicationBlocked")
                        )
                )
            );
    }

    @Test
    void unblockUser_deletesBlock_returnsStatus_andNotifiesBothUsers() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);
        User otherUser = Mockito.mock(User.class);
        UserBlock existingBlock = Mockito.mock(UserBlock.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(currentUser.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);
        when(userService.findUserById(2L)).thenReturn(otherUser);

        when(
            userBlockRepository.findByBlockerIdAndBlockedId(1L, 2L)
        ).thenReturn(Optional.of(existingBlock));

        when(
            userBlockRepository.existsByBlockerIdAndBlockedId(1L, 2L)
        ).thenReturn(false);

        when(
            userBlockRepository.existsByBlockerIdAndBlockedId(2L, 1L)
        ).thenReturn(false);

        Map<String, Object> response =
            chatController.unblockUser(2L, session);

        verify(userBlockRepository).delete(existingBlock);
        verify(userBlockRepository, never())
            .save(Mockito.any(UserBlock.class));

        assertAll(
            () -> assertFalse(
                (Boolean) response.get("blockedByCurrentUser")
            ),
            () -> assertFalse(
                (Boolean) response.get("blockedByOtherUser")
            ),
            () -> assertFalse(
                (Boolean) response.get("communicationBlocked")
            )
        );

        verify(messagingTemplate)
            .convertAndSendToUser(
                eq("1"),
                eq("/queue/block-status"),
                Mockito.<Map<String, Object>>argThat(status ->
                    Long.valueOf(2L).equals(status.get("otherUserId"))
                        && Boolean.FALSE.equals(
                            status.get("communicationBlocked")
                        )
                )
            );

        verify(messagingTemplate)
            .convertAndSendToUser(
                eq("2"),
                eq("/queue/block-status"),
                Mockito.<Map<String, Object>>argThat(status ->
                    Long.valueOf(1L).equals(status.get("otherUserId"))
                        && Boolean.FALSE.equals(
                            status.get("communicationBlocked")
                        )
                )
            );
    }
}