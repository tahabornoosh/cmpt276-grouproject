package com.cmpt276.group3.grouproject.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("""
        SELECT message
        FROM ChatMessage message
        WHERE (
            message.sender.id = :firstUserId
            AND message.recipient.id = :secondUserId
        )
        OR (
            message.sender.id = :secondUserId
            AND message.recipient.id = :firstUserId
        )
        ORDER BY message.sentAt ASC
            """)
    List<ChatMessage> findConversation(
        @Param("firstUserId") long firstUserId,
        @Param("secondUserId") long secondUserId
    );
}
