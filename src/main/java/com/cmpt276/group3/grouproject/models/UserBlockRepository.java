package com.cmpt276.group3.grouproject.models;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    Optional<UserBlock> findByBlockerIdAndBlockedId(
        Long blockerId,
        Long blockedId
    );
}
