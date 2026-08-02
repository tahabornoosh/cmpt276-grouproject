package com.cmpt276.group3.grouproject.models;

import java.time.LocalDateTime;

import com.cmpt276.group3.grouproject.enums.GroupRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Join table between User and FriendGroup, carrying the user's role in that group.
// The unique constraint is what stops a user joining the same group twice.
@Entity
@Table(
    name = "group_memberships",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_membership_group_user",
            columnNames = {"group_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_membership_group", columnList = "group_id"),
        @Index(name = "idx_membership_user", columnList = "user_id")
    }
)
public class GroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private FriendGroup group;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public GroupMembership() {
    }

    public GroupMembership(FriendGroup group, User user, GroupRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    @PrePersist
    void setJoinedAt() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FriendGroup getGroup() {
        return group;
    }

    public void setGroup(FriendGroup group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GroupRole getRole() {
        return role;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isAdmin() {
        return role == GroupRole.ADMIN;
    }
}
