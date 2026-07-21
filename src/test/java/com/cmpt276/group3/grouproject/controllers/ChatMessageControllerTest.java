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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UserBlock;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.ContactResponse;
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

    @Test
    void loadChat_loadsCurrentUsersConversations() {
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        User currentUser = Mockito.mock(User.class);
        ContactResponse contact = Mockito.mock(ContactResponse.class);
        List<ContactResponse> contacts = List.of(contact);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(
            chatMessageService.getExistingConversations(currentUser)
        ).thenReturn(contacts);

        String view = chatController.loadChat(2L, session, model);

        assertEquals("chat", view);

        verify(model).addAttribute("requestedUserId", 2L);
        verify(model).addAttribute("currentUser", currentUser);
        verify(model).addAttribute("contacts", contacts);
    }

    @Test
    void loadChat_redirectsToLoginWhenNotAuthenticated() {
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);

        when(auth.isLoggedIn(session)).thenReturn(false);

        String view = chatController.loadChat(null, session, model);

        assertEquals("redirect:/login", view);
        verifyNoInteractions(chatMessageService);
    }

    @Test
    void getConversation_returnsMessagesFromService() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);
        MessageResponse message = Mockito.mock(MessageResponse.class);
        List<MessageResponse> expectedMessages = List.of(message);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(
            chatMessageService.getConversation(currentUser, 2L)
        ).thenReturn(expectedMessages);

        List<MessageResponse> actualMessages =
            chatController.getConversation(2L, session);

        assertSame(expectedMessages, actualMessages);

        verify(chatMessageService)
            .getConversation(currentUser, 2L);
    }

    @Test
    void markConversationAsRead_delegatesToService() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);

        chatController.markConversationAsRead(2L, session);

        verify(chatMessageService)
            .markConversationAsRead(currentUser, 2L);
    }

    @Test
    void getBlockStatus_returnsBothBlockDirections() {
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
        ).thenReturn(true);

        when(
            userBlockRepository.existsByBlockerIdAndBlockedId(2L, 1L)
        ).thenReturn(false);

        Map<String, Object> status =
            chatController.getBlockStatus(2L, session);

        assertAll(
            () -> assertEquals(2L, status.get("otherUserId")),
            () -> assertTrue(
                (Boolean) status.get("blockedByCurrentUser")
            ),
            () -> assertFalse(
                (Boolean) status.get("blockedByOtherUser")
            ),
            () -> assertTrue(
                (Boolean) status.get("communicationBlocked")
            )
        );
    }

    @Test
    void chatApi_rejectsUnauthenticatedUser() {
        HttpSession session = Mockito.mock(HttpSession.class);

        when(auth.isLoggedIn(session)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> chatController.getConversation(2L, session)
        );

        assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
        );

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void blockUser_rejectsBlockingSelf() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(currentUser.getId()).thenReturn(1L);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> chatController.blockUser(1L, session)
        );

        assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
        );

        verifyNoInteractions(
            userService,
            userBlockRepository,
            messagingTemplate
        );
    }

    @Test
    void blockUser_rejectsUnknownOtherUser() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(currentUser.getId()).thenReturn(1L);
        when(userService.findUserById(999L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> chatController.blockUser(999L, session)
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        verifyNoInteractions(
            userBlockRepository,
            messagingTemplate
        );
    }

    @Test
    void blockUser_doesNotSaveDuplicateBlock() {
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
        ).thenReturn(true, true, true, true);

        when(
            userBlockRepository.existsByBlockerIdAndBlockedId(2L, 1L)
        ).thenReturn(false, false, false);

        Map<String, Object> status =
            chatController.blockUser(2L, session);

        verify(userBlockRepository, never())
            .save(Mockito.any(UserBlock.class));

        assertTrue(
            (Boolean) status.get("blockedByCurrentUser")
        );
    }

    @Test
    void sendMessage_whenCommunicationBlocked_sendsErrorToSender() {
        User sender = Mockito.mock(User.class);
        Principal principal = () -> "1";
        SendMessageRequest request =
            new SendMessageRequest(2L, "Hello");

        when(userService.findUserById(1L)).thenReturn(sender);
        when(
            chatMessageService.createMessage(sender, 2L, "Hello")
        ).thenThrow(
            new IllegalStateException("Communication is blocked")
        );

        chatController.sendMessage(request, principal);

        verify(messagingTemplate)
            .convertAndSendToUser(
                eq("1"),
                eq("/queue/chat-errors"),
                Mockito.<Map<String, Object>>argThat(error ->
                    "CHAT_BLOCKED".equals(error.get("code"))
                        && "Communication is blocked".equals(
                            error.get("message")
                        )
                )
            );
    }

}