package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.MessageResponse;
import com.cmpt276.group3.grouproject.util.SendMessageRequest;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private Auth auth;

    @Mock
    private UserService userService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    @Test
    void sendMessage_sendsResponseToSenderAndRecipient() {
        User sender = org.mockito.Mockito.mock(User.class);
        User recipient = org.mockito.Mockito.mock(User.class);
        ChatMessage savedMessage =
            org.mockito.Mockito.mock(ChatMessage.class);

        Principal principal = () -> "1";

        SendMessageRequest request =
            new SendMessageRequest(
                2L,
                "Hello"
            );

        Instant sentAt =
            Instant.parse(
                "2026-07-18T03:10:00Z"
            );

        when(userService.findUserById(1L))
            .thenReturn(sender);

        when(sender.getId())
            .thenReturn(1L);

        when(recipient.getId())
            .thenReturn(2L);

        when(
            chatMessageService.createMessage(
                sender,
                2L,
                "Hello"
            )
        ).thenReturn(savedMessage);

        when(savedMessage.getId())
            .thenReturn(100L);

        when(savedMessage.getSender())
            .thenReturn(sender);

        when(savedMessage.getRecipient())
            .thenReturn(recipient);

        when(savedMessage.getContent())
            .thenReturn("Hello");

        when(savedMessage.getSentAt())
            .thenReturn(sentAt);

        chatController.sendMessage(
            request,
            principal
        );

        ArgumentCaptor<MessageResponse> responseCaptor =
            ArgumentCaptor.forClass(
                MessageResponse.class
            );

        verify(chatMessageService)
            .createMessage(
                sender,
                2L,
                "Hello"
            );

        verify(messagingTemplate)
            .convertAndSendToUser(
                Mockito.eq("2"),
                Mockito.eq("/queue/messages"),
                responseCaptor.capture()
            );

        MessageResponse response =
            responseCaptor.getValue();

        org.junit.jupiter.api.Assertions.assertAll(
            () -> org.junit.jupiter.api.Assertions.assertEquals(
                100L,
                response.id()
            ),
            () -> org.junit.jupiter.api.Assertions.assertEquals(
                1L,
                response.senderId()
            ),
            () -> org.junit.jupiter.api.Assertions.assertEquals(
                2L,
                response.recipientId()
            ),
            () -> org.junit.jupiter.api.Assertions.assertEquals(
                "Hello",
                response.content()
            ),
            () -> org.junit.jupiter.api.Assertions.assertEquals(
                sentAt,
                response.sentAt()
            )
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
        SendMessageRequest request =
            new SendMessageRequest(
                2L,
                "Hello"
            );

        assertThrows(
            IllegalArgumentException.class,
            () -> chatController.sendMessage(
                request,
                null
            )
        );

        verifyNoInteractions(
            userService,
            chatMessageService,
            messagingTemplate
        );
    }
}