package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.Sport;
import com.cmpt276.group3.grouproject.enums.StudyField;
import com.cmpt276.group3.grouproject.enums.Venue;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;
import com.cmpt276.group3.grouproject.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(ProfileController.class)
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


    private MockHttpServletRequestBuilder validBasicQuestionnairePost() {
        return post("/questionnaire")
                .session(session)
                .param("age", "21")
                .param("year_of_study", "2")
                .param("study_field", StudyField.values()[0].name())
                .param("has_job", "false")
                .param("regularly_goes_to_gym", "false")
                .param("favourite_sport", Sport.values()[0].name())
                .param("preferred_venue", Venue.values()[0].name())
                .param("hobby1", Hobby.values()[0].name())
                .param("hobby2", Hobby.values()[0].name())
                .param("hobby3", Hobby.values()[0].name())
                .param("hobby4", Hobby.values()[0].name())
                .param("hobby5", Hobby.values()[0].name());
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
                .andExpect(model().attribute("isOwnProfile", true));
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

    @Test
    void questionnaire_redirectsToLoginWhenNotAuthenticated()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(get("/questionnaire").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void questionnaire_redirectsToProfileWhenAlreadyCompleted()
            throws Exception {

        MatchingProfile profile = new MatchingProfile();
        profile.setUser(loggedInUser);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(matchingProfileService.getProfileByUser(loggedInUser))
                .thenReturn(profile);

        mockMvc.perform(get("/questionnaire").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/1"));
    }

    @Test
    void questionnaire_editLoadsExistingProfile()
            throws Exception {

        MatchingProfile profile = new MatchingProfile();
        profile.setUser(loggedInUser);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(matchingProfileService.getProfileByUser(loggedInUser))
                .thenReturn(profile);

        mockMvc.perform(
                get("/questionnaire")
                        .session(session)
                        .param("edit", "1")
            )
            .andExpect(status().isOk())
            .andExpect(view().name("questionnaire"))
            .andExpect(model().attribute("currentUser", loggedInUser))
            .andExpect(model().attribute("matchingProfile", profile))
            .andExpect(model().attribute("hasQuestionnaire", true));
    }

    @Test
    void saveQuestionnaire_rejectsIncompleteBasicProfile()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);

        mockMvc.perform(
                post("/questionnaire")
                        .session(session)
                        .param("age", "21")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "/questionnaire?edit=1&basicIncomplete=1"
            ));
    }

    @Test
    void saveQuestionnaire_rejectsIncompleteFriendshipSection()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);

        mockMvc.perform(
                validBasicQuestionnairePost()
                        .param("display_friendship_profile", "on")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "/questionnaire?edit=1&friendshipIncomplete=1"
            ));
    }

    @Test
    void saveQuestionnaire_rejectsIncompleteRelationshipSection()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);

        mockMvc.perform(
                validBasicQuestionnairePost()
                        .param("display_dating_profile", "on")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "/questionnaire?edit=1&relationshipIncomplete=1"
            ));
    }

    @Test
    void saveQuestionnaire_rejectsIncompleteStudyBuddySection()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);

        mockMvc.perform(
                validBasicQuestionnairePost()
                        .param("display_study_buddy_profile", "on")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "/questionnaire?edit=1&studyBuddyIncomplete=1"
            ));
    }

    @Test
    void saveQuestionnaire_registersValidBasicProfile()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(loggedInUser);
        when(
            matchingProfileService.profileForUserExists(loggedInUser)
        ).thenReturn(false);

        mockMvc.perform(validBasicQuestionnairePost())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                    "/profile/1?questionnaireSaved=1"
                ));

        verify(matchingProfileService)
                .registerProfile(any(MatchingProfile.class));
    }

}
