package com.cmpt276.group3.grouproject.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MatchingProfileRepository extends JpaRepository<MatchingProfile, Integer> {
    public Optional<MatchingProfile> findByUser(User user);
    public Optional<MatchingProfile> findByUser_Id(long user_Id);
    boolean existsByUser(User user);
    boolean existsByUser_Id(Long id);
}
