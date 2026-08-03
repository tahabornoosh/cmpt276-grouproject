package com.cmpt276.group3.grouproject.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.FriendGroupRepository;
import com.cmpt276.group3.grouproject.models.GroupChatMessage;
import com.cmpt276.group3.grouproject.models.GroupChatMessageRepository;
import com.cmpt276.group3.grouproject.models.GroupMembershipRepository;
import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.util.GroupMessageResponse;

import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupChatMessageService {
    private static final int MAX_MESSAGE_LENGTH = 300;

    private final GroupChatMessageRepository groupChatMessageRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public GroupChatMessageService(
        GroupChatMessageRepository groupChatMessageRepository,
        FriendGroupRepository friendGroupRepository,
        GroupMembershipRepository groupMembershipRepository
    ) {
        this.groupChatMessageRepository = groupChatMessageRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Transactional(readOnly = true)
    public List<GroupMessageResponse> getMessages(
        User currentUser,
        Long groupId
    ) {
        requireCurrentUser(currentUser);

        FriendGroup group = requireGroup(groupId);
        requireApprovedGroupMember(group, currentUser);

        return groupChatMessageRepository
            .findByGroupOrderBySentAtAsc(group)
            .stream()
            .map(GroupMessageResponse::from)
            .toList();
    }

    @Transactional
    public GroupChatMessage createMessage(
        User sender,
        Long groupId,
        String content
    ) {
        requireCurrentUser(sender);

        FriendGroup group = requireGroup(groupId);
        requireApprovedGroupMember(group, sender);

        String cleanedContent = validateAndCleanContent(content);

        GroupChatMessage message = new GroupChatMessage();
        message.setGroup(group);
        message.setSender(sender);
        message.setContent(cleanedContent);

        return groupChatMessageRepository.save(message);
    }
    
    private User requireCurrentUser(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException(
                "The current user is required."
            );
        }
        return currentUser;
    }

    private FriendGroup requireGroup(Long groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException(
                "The group ID is required"
            );
        }

        return friendGroupRepository
            .findById(groupId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "The group was not found."
                )
        );
    }

    private void requireApprovedGroupMember(
        FriendGroup group,
        User user
    ) {
        boolean isApprovedMember = groupMembershipRepository
            .findByGroupAndUser(group, user)
            .map(membership -> membership.getRole() != GroupRole.PENDING)
            .orElse(false);

        if (!isApprovedMember) {
            throw new IllegalStateException(
                "You must be an approved member of this group"
            );
        }
    }

    private String validateAndCleanContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                "The message cannot be empty."
            );
        }

        String cleanedContent = content.trim();

        if (cleanedContent.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                "The message cannot exceed "+ MAX_MESSAGE_LENGTH + " characters."
            );
        }

        return cleanedContent;
    }
}
