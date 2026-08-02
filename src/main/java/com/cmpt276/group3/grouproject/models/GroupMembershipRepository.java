package com.cmpt276.group3.grouproject.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmpt276.group3.grouproject.enums.GroupRole;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    List<GroupMembership> findByUserOrderByJoinedAtDesc(User user);

    List<GroupMembership> findByGroupOrderByJoinedAtAsc(FriendGroup group);

    Optional<GroupMembership> findByGroupAndUser(FriendGroup group, User user);

    boolean existsByGroupAndUser(FriendGroup group, User user);

    long countByGroup(FriendGroup group);

    long countByGroupAndRole(FriendGroup group, GroupRole role);

    void deleteByGroup(FriendGroup group);
}
