package com.cmpt276.group3.grouproject.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.FriendGroupRepository;
import com.cmpt276.group3.grouproject.models.GroupChatMessage;
import com.cmpt276.group3.grouproject.models.GroupChatMessageRepository;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.models.GroupMembershipRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.util.GroupMessageResponse;

@ExtendWith(MockitoExtension.class)
class GroupChatMessageServiceTest {

    @Mock
    private GroupChatMessageRepository groupChatMessageRepository;

    @Mock
    private FriendGroupRepository friendGroupRepository;

    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    @InjectMocks
    private GroupChatMessageService groupChatMessageService;

    @Test
    void createMessage_savesTrimmedMessage() {
        User sender = Mockito.mock(User.class);
        FriendGroup group = Mockito.mock(FriendGroup.class);

        GroupMembership membership =
            new GroupMembership(group, sender, GroupRole.MEMBER);

        when(friendGroupRepository.findById(10L))
            .thenReturn(Optional.of(group));

        when(groupMembershipRepository.findByGroupAndUser(group, sender))
            .thenReturn(Optional.of(membership));

        when(groupChatMessageRepository.save(any(GroupChatMessage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        GroupChatMessage result = groupChatMessageService.createMessage(
            sender,
            10L,
            "  Hello group  "
        );

        ArgumentCaptor<GroupChatMessage> captor =
            ArgumentCaptor.forClass(GroupChatMessage.class);

        verify(groupChatMessageRepository).save(captor.capture());

        GroupChatMessage savedMessage = captor.getValue();

        assertAll(
            () -> assertSame(group, savedMessage.getGroup()),
            () -> assertSame(sender, savedMessage.getSender()),
            () -> assertEquals("Hello group", savedMessage.getContent()),
            () -> assertSame(savedMessage, result)
        );
    }

    @Test
    void createMessage_rejectsMissingUser() {
        assertThrows(
            IllegalArgumentException.class,
            () -> groupChatMessageService.createMessage(
                null,
                10L,
                "Hello"
            )
        );

        verifyNoInteractions(
            friendGroupRepository,
            groupMembershipRepository,
            groupChatMessageRepository
        );
    }

    @Test
    void createMessage_rejectsMissingOrUnknownGroup() {
        User sender = Mockito.mock(User.class);

        assertThrows(
            IllegalArgumentException.class,
            () -> groupChatMessageService.createMessage(
                sender,
                null,
                "Hello"
            )
        );

        when(friendGroupRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> groupChatMessageService.createMessage(
                sender,
                99L,
                "Hello"
            )
        );

        verify(
            groupChatMessageRepository,
            never()
        ).save(any(GroupChatMessage.class));
    }

    @Test
    void createMessage_rejectsNonMember() {
        User sender = Mockito.mock(User.class);
        FriendGroup group = Mockito.mock(FriendGroup.class);

        when(friendGroupRepository.findById(10L))
            .thenReturn(Optional.of(group));

        when(groupMembershipRepository.findByGroupAndUser(group, sender))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalStateException.class,
            () -> groupChatMessageService.createMessage(
                sender,
                10L,
                "Hello"
            )
        );

        verify(
            groupChatMessageRepository,
            never()
        ).save(any(GroupChatMessage.class));
    }

    @Test
    void createMessage_rejectsBlankAndLongContent() {
        User sender = Mockito.mock(User.class);
        FriendGroup group = Mockito.mock(FriendGroup.class);

        GroupMembership membership =
            new GroupMembership(group, sender, GroupRole.MEMBER);

        when(friendGroupRepository.findById(10L))
            .thenReturn(Optional.of(group));

        when(groupMembershipRepository.findByGroupAndUser(group, sender))
            .thenReturn(Optional.of(membership));

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> groupChatMessageService.createMessage(
                    sender,
                    10L,
                    null
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> groupChatMessageService.createMessage(
                    sender,
                    10L,
                    "   "
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> groupChatMessageService.createMessage(
                    sender,
                    10L,
                    "a".repeat(301)
                )
            )
        );

        verify(
            groupChatMessageRepository,
            never()
        ).save(any(GroupChatMessage.class));
    }

    @Test
    void getMessages_returnsMessagesInRepositoryOrder() {
        User currentUser = Mockito.mock(User.class);
        User firstSender = Mockito.mock(User.class);
        User secondSender = Mockito.mock(User.class);
        FriendGroup group = Mockito.mock(FriendGroup.class);

        GroupMembership membership =
            new GroupMembership(group, currentUser, GroupRole.MEMBER);

        when(group.getId()).thenReturn(10L);

        when(firstSender.getId()).thenReturn(1L);
        when(firstSender.getFirst_name()).thenReturn("First");
        when(firstSender.getLast_name()).thenReturn("User");

        when(secondSender.getId()).thenReturn(2L);
        when(secondSender.getFirst_name()).thenReturn("Second");
        when(secondSender.getLast_name()).thenReturn("User");

        GroupChatMessage firstMessage =
            new GroupChatMessage(group, firstSender, "First message");
        firstMessage.setId(101L);

        GroupChatMessage secondMessage =
            new GroupChatMessage(group, secondSender, "Second message");
        secondMessage.setId(102L);

        when(friendGroupRepository.findById(10L))
            .thenReturn(Optional.of(group));

        when(
            groupMembershipRepository.findByGroupAndUser(
                group,
                currentUser
            )
        ).thenReturn(Optional.of(membership));

        when(groupChatMessageRepository.findByGroupOrderBySentAtAsc(group))
            .thenReturn(List.of(firstMessage, secondMessage));

        List<GroupMessageResponse> responses =
            groupChatMessageService.getMessages(currentUser, 10L);

        assertEquals(2, responses.size());

        assertAll(
            () -> assertEquals(101L, responses.get(0).id()),
            () -> assertEquals(10L, responses.get(0).groupId()),
            () -> assertEquals(1L, responses.get(0).senderId()),
            () -> assertEquals(
                "First User",
                responses.get(0).senderName()
            ),
            () -> assertEquals(
                "First message",
                responses.get(0).content()
            ),
            () -> assertEquals(102L, responses.get(1).id()),
            () -> assertEquals(2L, responses.get(1).senderId()),
            () -> assertEquals(
                "Second User",
                responses.get(1).senderName()
            ),
            () -> assertEquals(
                "Second message",
                responses.get(1).content()
            )
        );

        verify(groupChatMessageRepository)
            .findByGroupOrderBySentAtAsc(group);
    }

    @Test
    void getMessages_rejectsNonMember() {
        User currentUser = Mockito.mock(User.class);
        FriendGroup group = Mockito.mock(FriendGroup.class);

        when(friendGroupRepository.findById(10L))
            .thenReturn(Optional.of(group));

        when(
            groupMembershipRepository.findByGroupAndUser(
                group,
                currentUser
            )
        ).thenReturn(Optional.empty());

        assertThrows(
            IllegalStateException.class,
            () -> groupChatMessageService.getMessages(
                currentUser,
                10L
            )
        );

        verify(
            groupChatMessageRepository,
            never()
        ).findByGroupOrderBySentAtAsc(any(FriendGroup.class));
    }
}