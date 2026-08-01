package com.cmpt276.group3.grouproject.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendGroupRepository extends JpaRepository<FriendGroup, Long> {

    // Public browse list - only groups still accepting members.
    List<FriendGroup> findByOpenToNewMembersTrueOrderByCreatedAtDesc();

    List<FriendGroup> findByCreatedBy(User createdBy);

    boolean existsByNameIgnoreCase(String name);
}