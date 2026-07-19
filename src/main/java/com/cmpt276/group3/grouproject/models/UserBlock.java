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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "user_blocks",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_blocks_blocker_blocked",
        columnNames = {"blocker_id", "blocked_id"}
    ),
    indexes = {
        @Index(name = "idx_user_blocks_blocker", columnList = "blocker_id"),
        @Index(name = "idx_user_blocks_blocked", columnList = "blocked_id")
    }
)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected UserBlock() {
    }

    public UserBlock(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
    }

    public Long getId() {
        return id;
    }

    public User getBlocker() {
        return blocker;
    }

    public User getBlocked() {
        return blocked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
