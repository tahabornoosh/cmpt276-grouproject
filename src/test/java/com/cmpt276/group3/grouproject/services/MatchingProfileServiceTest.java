package com.cmpt276.group3.grouproject.services;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MatchingProfileServiceTest {

    @Autowired
    private MatchingProfileService matchingProfileService;

    @MockitoBean
    private MatchingProfileRepository matchingProfileRepository;

    private User testUser;
    private MatchingProfile validProfile;

    @BeforeEach
    void setUp() {
        testUser = new User("Test", "User", "test@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        testUser.setId(1L);

        // A minimal valid profile with no active sub-profiles enabled,
        // so ProfileValidator.validateActiveProfiles passes without requiring extra fields.
        validProfile = new MatchingProfile();
        validProfile.setUser(testUser);
        validProfile.setAge(20);
        validProfile.setDisplay_friendship_profile(false);
        validProfile.setDisplay_dating_profile(false);
        validProfile.setDisplay_study_buddy_profile(false);
    }

    // --- Task 2: Questionnaire must load (getProfileByUser returns correctly) ---

    @Test
    void getProfileByUser_returnsProfileWhenExists() {
        when(matchingProfileRepository.findByUser(testUser)).thenReturn(Optional.of(validProfile));

        MatchingProfile result = matchingProfileService.getProfileByUser(testUser);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(matchingProfileRepository).findByUser(testUser);
    }

    @Test
    void getProfileByUser_returnsNullWhenNoProfileExists() {
        when(matchingProfileRepository.findByUser(testUser)).thenReturn(Optional.empty());

        MatchingProfile result = matchingProfileService.getProfileByUser(testUser);

        assertNull(result);
        verify(matchingProfileRepository).findByUser(testUser);
    }

    @Test
    void getProfileByUser_returnsNullForNullUser() {
        MatchingProfile result = matchingProfileService.getProfileByUser(null);

        assertNull(result);
        // Should not even touch the repository
        verify(matchingProfileRepository, never()).findByUser(any());
    }

    // --- Task 2: Questionnaire must accept a valid input ---

    @Test
    void registerProfile_acceptsValidInput() {
        when(matchingProfileRepository.existsByUser(testUser)).thenReturn(false);
        when(matchingProfileRepository.save(any(MatchingProfile.class)))
                .thenAnswer(invocation -> {
                    MatchingProfile saved = invocation.getArgument(0);
                    saved.setId(1);
                    return saved;
                });

        MatchingProfile result = matchingProfileService.registerProfile(validProfile);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(matchingProfileRepository).save(validProfile);
    }

    @Test
    void registerProfile_acceptsValidInputWithFriendshipEnabled() {
        // Friendship profile validation is currently empty in ProfileValidator,
        // so enabling it should still pass without any additional required fields.
        validProfile.setDisplay_friendship_profile(true);

        when(matchingProfileRepository.existsByUser(testUser)).thenReturn(false);
        when(matchingProfileRepository.save(any(MatchingProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchingProfile result = matchingProfileService.registerProfile(validProfile);

        assertNotNull(result);
        assertTrue(result.isDisplay_friendship_profile());
        verify(matchingProfileRepository).save(validProfile);
    }

    @Test
    void registerProfile_acceptsValidInputWithDatingEnabled() {
        // Dating profile validation requires min/max partner age and relationship type
        validProfile.setDisplay_dating_profile(true);
        validProfile.setMin_partner_age(18);
        validProfile.setMax_partner_age(30);
        validProfile.setLooking_for_short_term_relationship(false);

        when(matchingProfileRepository.existsByUser(testUser)).thenReturn(false);
        when(matchingProfileRepository.save(any(MatchingProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchingProfile result = matchingProfileService.registerProfile(validProfile);

        assertNotNull(result);
        assertTrue(result.isDisplay_dating_profile());
        verify(matchingProfileRepository).save(validProfile);
    }

    // --- Questionnaire must reject invalid input ---

    @Test
    void registerProfile_rejectsNullProfile() {
        assertThrows(IllegalArgumentException.class, () -> {
            matchingProfileService.registerProfile(null);
        });
        verify(matchingProfileRepository, never()).save(any());
    }

    @Test
    void registerProfile_rejectsProfileWithNoUser() {
        MatchingProfile noUserProfile = new MatchingProfile();

        assertThrows(IllegalArgumentException.class, () -> {
            matchingProfileService.registerProfile(noUserProfile);
        });
        verify(matchingProfileRepository, never()).save(any());
    }

    @Test
    void registerProfile_rejectsDuplicateProfileForSameUser() {
        when(matchingProfileRepository.existsByUser(testUser)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            matchingProfileService.registerProfile(validProfile);
        });
        verify(matchingProfileRepository, never()).save(any());
    }

    @Test
    void registerProfile_rejectsDatingProfileMissingRequiredFields() {
        // Dating profile enabled but missing min/max age - should fail validation
        validProfile.setDisplay_dating_profile(true);
        // min_partner_age and max_partner_age left null intentionally

        when(matchingProfileRepository.existsByUser(testUser)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            matchingProfileService.registerProfile(validProfile);
        });
        verify(matchingProfileRepository, never()).save(any());
    }

    // --- Update profile tests ---

    @Test
    void updateProfile_acceptsValidUpdate() {
        validProfile.setId(1);
        when(matchingProfileRepository.findByUser(testUser)).thenReturn(Optional.of(validProfile));
        when(matchingProfileRepository.save(any(MatchingProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MatchingProfile updatedProfile = new MatchingProfile();
        updatedProfile.setUser(testUser);
        updatedProfile.setAge(25);
        updatedProfile.setDisplay_friendship_profile(false);
        updatedProfile.setDisplay_dating_profile(false);
        updatedProfile.setDisplay_study_buddy_profile(false);

        MatchingProfile result = matchingProfileService.updateProfile(updatedProfile);

        assertNotNull(result);
        assertEquals(25, result.getAge());
        verify(matchingProfileRepository).save(any(MatchingProfile.class));
    }

    @Test
    void updateProfile_rejectsUpdateForNonExistentProfile() {
        when(matchingProfileRepository.findByUser(testUser)).thenReturn(Optional.empty());

        MatchingProfile profileToUpdate = new MatchingProfile();
        profileToUpdate.setUser(testUser);

        assertThrows(IllegalArgumentException.class, () -> {
            matchingProfileService.updateProfile(profileToUpdate);
        });
        verify(matchingProfileRepository, never()).save(any());
    }
}
