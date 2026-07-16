package com.cmpt276.group3.grouproject.models;

import com.cmpt276.group3.grouproject.enums.EOIStream;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "expressions_of_interest",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_eoi_sender_receiver_stream",
            columnNames = {"sender_id", "receiver_id", "stream"}
        )
    },
    indexes = {
        @Index(name = "idx_eoi_receiver", columnList = "receiver_id")
    }
)
public class ExpressionOfInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EOIStream stream;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ExpressionOfInterest() {
    }

    public ExpressionOfInterest(User sender, User receiver, EOIStream stream) {
        this.sender = sender;
        this.receiver = receiver;
        this.stream = stream;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public EOIStream getStream() {
        return stream;
    }

    public void setStream(EOIStream stream) {
        this.stream = stream;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
