package com.cmpt276.group3.grouproject.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MatchingProfileRepository extends JpaRepository<MatchingProfile, Integer> {
    public Optional<MatchingProfile> findByUser(User user);
    public Optional<MatchingProfile> findByUser_Id(long user_Id);
    boolean existsByUser(User user);
    boolean existsByUser_Id(Long id);

    // Candidates for the Feeds page: everyone who has completed and enabled a given stream.
    List<MatchingProfile> findByDisplay_friendship_profile(boolean value);
    List<MatchingProfile> findByDisplay_dating_profile(boolean value);
    List<MatchingProfile> findByDisplay_study_buddy_profile(boolean value);
}
