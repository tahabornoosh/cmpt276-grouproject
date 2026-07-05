package com.cmpt276.group3.grouproject.services;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;

@Service
public class MatchingProfileService {
    private final MatchingProfileRepository matchingProfileRepository;

    public MatchingProfileService(MatchingProfileRepository matchingProfileRepository) {
        this.matchingProfileRepository = matchingProfileRepository;
    }

    public MatchingProfile registerProfile(MatchingProfile newProfile) {
        if (newProfile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }

        if (newProfile.getUser() == null) {
            throw new IllegalArgumentException("Profile must be linked to a user");
        }
        
        if (profileForUserExists(newProfile.getUser())) {
            throw new IllegalArgumentException("Profile for user already exists");
        }

        // Throws error upon invalidated profile
        ProfileValidator.validateActiveProfiles(newProfile);
        //

        return matchingProfileRepository.save(newProfile);
    }

    //

    public MatchingProfile updateProfile(MatchingProfile targetProfile) {
        if (targetProfile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }

        if (targetProfile.getUser() == null) {
            throw new IllegalArgumentException("Profile must be linked to a user");
        }
        
        MatchingProfile loadProfile = getProfileByUser(targetProfile.getUser());

        if (loadProfile == null) {
            throw new IllegalArgumentException("Unable to find existing profile with user");
        }

        updateEditableFields(loadProfile, targetProfile);

        ProfileValidator.validateActiveProfiles(loadProfile);
        return matchingProfileRepository.save(loadProfile);
    }

    // Helpers

    private void updateEditableFields(MatchingProfile toUpdate, MatchingProfile updateFrom) {
        toUpdate.setDisplay_dating_profile(updateFrom.isDisplay_dating_profile());
        toUpdate.setDisplay_friendship_profile(updateFrom.isDisplay_friendship_profile());
        toUpdate.setDisplay_study_buddy_profile(updateFrom.isDisplay_study_buddy_profile());

        // Basic Data
        toUpdate.setAge(updateFrom.getAge());
        toUpdate.setStudy_field(updateFrom.getStudy_field());
        toUpdate.setHas_job(updateFrom.isHas_job());
        toUpdate.setRegularly_goes_to_gym(updateFrom.isRegularly_goes_to_gym());
        toUpdate.setFavourite_sport(updateFrom.getFavourite_sport());
        toUpdate.setPreferred_venue(updateFrom.getPreferred_venue());
        toUpdate.setHobby1(updateFrom.getHobby1());
        toUpdate.setHobby2(updateFrom.getHobby2());
        toUpdate.setHobby3(updateFrom.getHobby3());
        toUpdate.setHobby4(updateFrom.getHobby4());
        toUpdate.setHobby5(updateFrom.getHobby5());

        // Friendship Data
        toUpdate.setKind_of_friendship(updateFrom.getKind_of_friendship());
        toUpdate.setSocial_style(updateFrom.getSocial_style());
        toUpdate.setHangout_frequency(updateFrom.getHangout_frequency());
        toUpdate.setFriend_activity(updateFrom.getFriend_activity());
        toUpdate.setPlanning_style(updateFrom.getPlanning_style());
        toUpdate.setConversation_style(updateFrom.getConversation_style());
        toUpdate.setCommunication_style(updateFrom.getCommunication_style());
        toUpdate.setPersonality_trait(updateFrom.getPersonality_trait());
        toUpdate.setFriendship_value(updateFrom.getFriendship_value());
        toUpdate.setAvailability(updateFrom.getAvailability());
        toUpdate.setMotivation(updateFrom.getMotivation());
        toUpdate.setFriend_type(updateFrom.getFriend_type());

        // Dating data
        toUpdate.setLooking_for_short_term_relationship(updateFrom.isLooking_for_short_term_relationship());
        toUpdate.setMin_partner_age(updateFrom.getMin_partner_age());
        toUpdate.setMax_partner_age(updateFrom.getMax_partner_age());
        toUpdate.setPartner_gender(updateFrom.getPartner_gender());

        // Buddy Data
        toUpdate.setBuddy_area_of_study(updateFrom.getBuddy_area_of_study());
        toUpdate.setBuddy_min_year_of_study(updateFrom.getBuddy_min_year_of_study());
        toUpdate.setBuddy_max_year_of_study(updateFrom.getBuddy_max_year_of_study());

    }

    public boolean profileForUserExists(User user) {
        if (user == null) {
            return false;
        }
        return matchingProfileRepository.existsByUser(user);
    }

    // Getters
    public MatchingProfile getProfileByUser(User user) {
        if (user == null) {
            return null;
        }
        return matchingProfileRepository.findByUser(user).orElse(null);
    }

    public MatchingProfile getProfileByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return matchingProfileRepository.findByUser_Id(userId).orElse(null);
    }
}
