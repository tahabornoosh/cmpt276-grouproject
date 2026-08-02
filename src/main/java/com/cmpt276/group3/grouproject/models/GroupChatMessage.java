package com.cmpt276.group3.grouproject.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "group_chat_messages",
    indexes = {
        @Index(
            name = "idx_group_chat_group_sent_at",
            columnList = "group_id, sent_at"
        )
    }
)
public class GroupChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private FriendGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(
        name = "sent_at",
        nullable = false,
        updatable = false
    )
    private Instant sentAt;

    public GroupChatMessage() {

    }

    public GroupChatMessage(
        FriendGroup group,
        User sender,
        String content
    ) {
        this.group = group;
        this.sender = sender;
        this.content = content;
        this.sentAt = Instant.now();
    }

    @PrePersist
    private void assignSentAt() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public FriendGroup getGroup() {
        return group;
    }

    public void setGroup(FriendGroup group) {
        this.group = group;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
