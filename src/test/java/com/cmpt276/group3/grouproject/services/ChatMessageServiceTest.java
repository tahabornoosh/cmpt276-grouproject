package com.cmpt276.group3.grouproject.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.ChatMessageRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;
import com.cmpt276.group3.grouproject.util.ContactResponse;
import com.cmpt276.group3.grouproject.util.MessageResponse;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserBlockRepository userBlockRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    void createMessage_savesValidMessage() {
        User sender = userWithId(1L);
        User recipient = userWithId(2L);

        when(userService.findUserById(2L))
            .thenReturn(recipient);

        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = chatMessageService.createMessage(
            sender,
            2L,
            "Hello! "
        );

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

        verify(chatMessageRepository).save(messageCaptor.capture());

        ChatMessage savedMessage = messageCaptor.getValue();

        assertAll(
            () -> assertSame(
                sender,
                savedMessage.getSender()
            ),
            () -> assertSame(
                recipient,
                savedMessage.getRecipient()
            ),
            () -> assertEquals(
                "Hello!",
                savedMessage.getContent()
            ),
            () -> assertSame(
                savedMessage,
                result
            )
        );
        
    }

    @Test
    void createMessage_rejectsInvalidRequests() {
        User sender = userWithId(1L);
        when(userService.findUserById(1L))
            .thenReturn(sender);
        
        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> chatMessageService.createMessage(
                    null,
                    2L,
                    "Hello"
                    )
                ),

            () -> assertThrows(
                IllegalArgumentException.class,
                () -> chatMessageService.createMessage(
                    sender,
                    null,
                    "Hello"
                    )
                ),

            () -> assertThrows(
                IllegalArgumentException.class,
                () -> chatMessageService.createMessage(
                    sender,
                    2L,
                    " "
                    )
                ),

            () -> assertThrows(
                IllegalArgumentException.class,
                () -> chatMessageService.createMessage(
                    sender,
                    2L,
                    "a".repeat(301)
                    )
                ),

            () -> assertThrows(
                IllegalArgumentException.class,
                () -> chatMessageService.createMessage(
                    sender,
                    1L,
                    "Hello"
                    )
                )

        );

        assertThrows(
                IllegalArgumentException.class,
                () -> chatMessageService.createMessage(
                    sender,
                    999L,
                    "Hello"
                    )
                );
        verify(
            chatMessageRepository,
            never()
        ).save(any(ChatMessage.class));
    }

    @Test
    void getConversation_returnsMessagesBetweenUsers() {
        User currentUser = userWithId(1L);
        User otherUser = userWithId(2L);

        when(userService.findUserById(2L))
            .thenReturn(otherUser);

        ChatMessage firstMessage =
            new ChatMessage(
                currentUser,
                otherUser,
                "Hello",
                false
            );

        firstMessage.setId(101L);

        ChatMessage secondMessage =
            new ChatMessage(
                otherUser,
                currentUser,
                "Hi",
                false
            );

        secondMessage.setId(102L);

        when(
            chatMessageRepository.findConversation(
                1L,
                2L
            )
        ).thenReturn(
            List.of(
                firstMessage,
                secondMessage
            )
        );

        List<MessageResponse> responses =
            chatMessageService.getConversation(
                currentUser,
                2L
            );

        assertEquals(2, responses.size());

        MessageResponse firstResponse =
            responses.get(0);

        MessageResponse secondResponse =
            responses.get(1);

        assertAll(
            () -> assertEquals(
                101L,
                firstResponse.id()
            ),
            () -> assertEquals(
                1L,
                firstResponse.senderId()
            ),
            () -> assertEquals(
                2L,
                firstResponse.recipientId()
            ),
            () -> assertEquals(
                "Hello",
                firstResponse.content()
            ),
            () -> assertEquals(
                firstMessage.getSentAt(),
                firstResponse.sentAt()
            ),

            () -> assertEquals(
                102L,
                secondResponse.id()
            ),
            () -> assertEquals(
                2L,
                secondResponse.senderId()
            ),
            () -> assertEquals(
                1L,
                secondResponse.recipientId()
            ),
            () -> assertEquals(
                "Hi",
                secondResponse.content()
            )
        );

        verify(chatMessageRepository)
            .findConversation(1L, 2L);
    }

    @Test
    void getExistingConversations_returnsLatestMessagePerUser() {
        User currentUser = userWithId(1L);
        User alice = userWithId(2L);
        User bob = userWithId(3L);

        when(alice.getFirst_name())
            .thenReturn("Alice");

        when(alice.getLast_name())
            .thenReturn("Smith");

        when(bob.getFirst_name())
            .thenReturn("Bob");

        when(bob.getLast_name())
            .thenReturn("Jones");

        /*
         * The repository returns messages newest first.
         * The older Alice message should therefore be ignored.
         */
        ChatMessage newestAliceMessage =
            new ChatMessage(
                currentUser,
                alice,
                "Newest Alice message",
                false
            );

        ChatMessage newestBobMessage =
            new ChatMessage(
                bob,
                currentUser,
                "Newest Bob message",
                false
            );

        ChatMessage olderAliceMessage =
            new ChatMessage(
                alice,
                currentUser,
                "Older Alice message",
                false
            );

        when(
            chatMessageRepository.findMessagesForUser(1L)
        ).thenReturn(
            List.of(
                newestAliceMessage,
                newestBobMessage,
                olderAliceMessage
            )
        );

        List<ContactResponse> contacts =
            chatMessageService
                .getExistingConversations(currentUser);

        assertEquals(2, contacts.size());

        ContactResponse aliceContact =
            contacts.get(0);

        ContactResponse bobContact =
            contacts.get(1);

        assertAll(
            () -> assertEquals(
                2L,
                aliceContact.userId()
            ),
            () -> assertEquals(
                "Alice",
                aliceContact.firstName()
            ),
            () -> assertEquals(
                "Smith",
                aliceContact.lastName()
            ),
            () -> assertEquals(
                "Newest Alice message",
                aliceContact.lastMessage()
            ),
            () -> assertEquals(
                newestAliceMessage.getSentAt(),
                aliceContact.lastMessageAt()
            ),
            () -> assertTrue(
                aliceContact
                    .lastMessageSentByCurrentUser()
            ),

            () -> assertEquals(
                3L,
                bobContact.userId()
            ),
            () -> assertEquals(
                "Bob",
                bobContact.firstName()
            ),
            () -> assertEquals(
                "Jones",
                bobContact.lastName()
            ),
            () -> assertEquals(
                "Newest Bob message",
                bobContact.lastMessage()
            ),
            () -> assertFalse(
                bobContact
                    .lastMessageSentByCurrentUser()
            )
        );

        verify(chatMessageRepository)
            .findMessagesForUser(1L);
    }

    private User userWithId(long id) {
        User user = Mockito.mock(User.class);

        when(user.getId()).thenReturn(id);

        return user;
    }
}
