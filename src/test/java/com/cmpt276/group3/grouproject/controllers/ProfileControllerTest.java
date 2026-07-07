package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;
import com.cmpt276.group3.grouproject.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Auth auth;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private MatchingProfileService matchingProfileService;

    @MockitoBean
    private UserService userService;

    private MockHttpSession session;
    private User loggedInUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        loggedInUser = new User("Pravit", "Hundal", "pravit@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        loggedInUser.setId(1L);

        targetUser = new User("Taha", "Bornoosh", "taha@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        targetUser.setId(2L);
    }

    // --- Task 3: Profile must load correctly for an existing user ---

    @Test
    void profilePage_loadsForExistingUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        // No questionnaire for this user - matchingProfileService returns null gracefully
        when(matchingProfileService.getProfileByUser(targetUser)).thenReturn(null);

        mockMvc.perform(get("/profile/2").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("profileUser", targetUser))
                .andExpect(model().attribute("hasQuestionnaire", false));
    }

    @Test
    void profilePage_loadsAndShowsMatchScoreWhenBothHaveFriendshipProfiles() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        MatchingProfile targetProfile = new MatchingProfile();
        targetProfile.setUser(targetUser);
        targetProfile.setDisplay_friendship_profile(true);
        targetProfile.setAge(21);

        MatchingProfile viewerProfile = new MatchingProfile();
        viewerProfile.setUser(loggedInUser);
        viewerProfile.setDisplay_friendship_profile(true);
        viewerProfile.setAge(22);

        when(matchingProfileService.getProfileByUser(targetUser)).thenReturn(targetProfile);
        when(matchingProfileService.getProfileByUser(loggedInUser)).thenReturn(viewerProfile);

        mockMvc.perform(get("/profile/2").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("hasQuestionnaire", true));
    }

    @Test
    void profilePage_noMatchScoreOnOwnProfile() throws Exception {
        // Viewing your own profile - match score must not appear
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(usersRepository.findById(1L)).thenReturn(Optional.of(loggedInUser));
        when(matchingProfileService.getProfileByUser(loggedInUser)).thenReturn(null);

        mockMvc.perform(get("/profile/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("isOwnProfile", true))
                .andExpect(model().attributeExists("matchScore"))
                // matchScore attribute must be null on own profile
                .andExpect(model().attribute("matchScore", org.hamcrest.Matchers.nullValue()));
    }

    // --- Task 3: Profile must redirect to dashboard for a non-existing user ---

    @Test
    void profilePage_redirectsForNonExistingUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(usersRepository.findById(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/profile/9999").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // --- Task 4: Profile must redirect to login for unauthenticated user ---

    @Test
    void profilePage_redirectsToLoginWhenNotAuthenticated() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(get("/profile/2").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
