package com.cmpt276.group3.grouproject.services;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.MatchingProfile;

@Service
public class ProfileValidator {
    public static void validateActiveProfiles(MatchingProfile profile) {
        if (profile.isDisplay_dating_profile()) {
            validateDatingProfile(profile);
        }

        if (profile.isDisplay_friendship_profile()) {
            validateFriendshipProfile(profile);
        }

        if (profile.isDisplay_study_buddy_profile()) {
            validateStudyProfile(profile);
        }
    }


    public static void validateDatingProfile(MatchingProfile profile) {
        if (profile.getMin_partner_age() == null) {
            throw new IllegalArgumentException("Minimum partner age required");
        }

        if (profile.getMax_partner_age() == null) {
            throw new IllegalArgumentException("Maximum partner age required");
        }

        if (profile.isLooking_for_short_term_relationship() == null) {
            throw new IllegalArgumentException("Looking for short term relationship required");
        }
    }

    public static void validateStudyProfile(MatchingProfile profile) {

    }

    public static void validateFriendshipProfile(MatchingProfile profile) {
        
    }
}
