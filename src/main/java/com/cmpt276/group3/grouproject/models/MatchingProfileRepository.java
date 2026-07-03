package com.cmpt276.group3.grouproject.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchingProfileRepository extends JpaRepository<MatchingProfile, Integer> {
    public Optional<MatchingProfile> getByUser(User user);
}
