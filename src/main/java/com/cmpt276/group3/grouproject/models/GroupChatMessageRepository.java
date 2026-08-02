package com.cmpt276.group3.grouproject.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long>{
    
    List<GroupChatMessage> findByGroupOrderBySentAtAsc(
        FriendGroup group
    );

    void deleteByGroup(FriendGroup group);
}
