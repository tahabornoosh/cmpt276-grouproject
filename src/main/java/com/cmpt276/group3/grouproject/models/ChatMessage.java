package com.cmpt276.group3.grouproject.models;

import java.time.Instant;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    @Nullable
    @Column(nullable = true)
    private Boolean unread;

    public ChatMessage() {

    }
    
    public ChatMessage(User sender, User recipient, String content, Boolean unread) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.sentAt = Instant.now();
        this.unread = unread;
    }

    public Long getId() {
        return id;
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

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
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

    @PrePersist
    private void assignSentAt() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }

    public boolean isUnread() {
        if (this.unread==null) return false;
        return this.unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }
}

