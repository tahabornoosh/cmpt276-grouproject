package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.enums.Sport;
import com.cmpt276.group3.grouproject.enums.StudyField;
import com.cmpt276.group3.grouproject.enums.Venue;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(FeedController.class)
public class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Auth auth;

    @MockitoBean
    private MatchingProfileService matchingProfileService;

    @MockitoBean
    private MatchingProfileRepository matchingProfileRepository;

    private MockHttpSession session;
    private User mike;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        mike = new User("Mike", "Chen", "mike@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        mike.setId(1L);
    }

    // --- Auth gate ---

    @Test
    void feed_redirectsToLoginWhenNotAuthenticated() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(get("/feeds/friendship").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void feed_redirectsHomeForUnknownStreamSlug() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(mike);

        mockMvc.perform(get("/feeds/not-a-real-stream").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // --- Acceptance criteria: stream not completed/enabled -> informative error, not a feed ---

    @Test
    void feed_showsErrorWhenMikeHasNoQuestionnaire() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(mike);
        when(matchingProfileService.getProfileByUser(mike)).thenReturn(null);

        mockMvc.perform(get("/feeds/relationship").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("feed"))
                .andExpect(model().attribute("streamIncomplete", true));
    }

    @Test
    void feed_showsErrorWhenStreamDisabled() throws Exception {
        MatchingProfile mikeProfile = new MatchingProfile();
        mikeProfile.setUser(mike);
        mikeProfile.setDisplay_study_buddy_profile(false);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(mike);
        when(matchingProfileService.getProfileByUser(mike)).thenReturn(mikeProfile);

        mockMvc.perform(get("/feeds/study-buddy").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("feed"))
                .andExpect(model().attribute("streamIncomplete", true));
    }

    // --- Acceptance criteria: completed + enabled stream -> matches, highest score first ---

    @Test
    void feed_showsMatchesSortedByScoreDescendingAndExcludesSelf() throws Exception {
        MatchingProfile mikeProfile = new MatchingProfile();
        mikeProfile.setUser(mike);
        mikeProfile.setDisplay_friendship_profile(true);
        mikeProfile.setAge(20);
        mikeProfile.setYear_of_study(2);
        mikeProfile.setStudy_field(StudyField.APPLIED_SCIENCE);
        mikeProfile.setRegularly_goes_to_gym(true);
        mikeProfile.setFavourite_sport(Sport.BASKETBALL);
        mikeProfile.setPreferred_venue(Venue.COFFEE_SHOP);
        mikeProfile.setHobby1(Hobby.CHESS);

        User closeMatchUser = new User("Ana", "Lee", "ana@sfu.ca", "pw", Role.USER, Gender.FEMALE, "");
        closeMatchUser.setId(2L);
        MatchingProfile closeMatchProfile = new MatchingProfile();
        closeMatchProfile.setUser(closeMatchUser);
        closeMatchProfile.setDisplay_friendship_profile(true);
        closeMatchProfile.setAge(20);
        closeMatchProfile.setYear_of_study(2);
        closeMatchProfile.setStudy_field(StudyField.APPLIED_SCIENCE);
        closeMatchProfile.setRegularly_goes_to_gym(true);
        closeMatchProfile.setFavourite_sport(Sport.BASKETBALL);
        closeMatchProfile.setPreferred_venue(Venue.COFFEE_SHOP);
        closeMatchProfile.setHobby1(Hobby.CHESS);

        User farMatchUser = new User("Sam", "Ray", "sam@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        farMatchUser.setId(3L);
        MatchingProfile farMatchProfile = new MatchingProfile();
        farMatchProfile.setUser(farMatchUser);
        farMatchProfile.setDisplay_friendship_profile(true);
        farMatchProfile.setAge(35);
        farMatchProfile.setYear_of_study(6);
        farMatchProfile.setStudy_field(StudyField.BUSINESS);
        farMatchProfile.setRegularly_goes_to_gym(false);
        farMatchProfile.setFavourite_sport(Sport.RUNNING);
        farMatchProfile.setPreferred_venue(Venue.PARK);
        farMatchProfile.setHobby1(Hobby.GARDENING);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(mike);
        when(matchingProfileService.getProfileByUser(mike)).thenReturn(mikeProfile);
        // Repository returns Mike's own profile too - the controller must filter it out.
        when(matchingProfileRepository.findAll())
                .thenReturn(List.of(mikeProfile, closeMatchProfile, farMatchProfile));

        var result = mockMvc.perform(get("/feeds/friendship").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("feed"))
                .andExpect(model().attribute("streamIncomplete", false))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<FeedController.FeedEntry> matches =
                (List<FeedController.FeedEntry>) result.getModelAndView().getModel().get("matches");

        assertEquals(2, matches.size(), "Mike should not be matched with himself");
        assertEquals("Ana", matches.get(0).getUser().getFirst_name(),
                "The closer profile (same age/year/field/hobbies) should rank first");
        assertEquals("Sam", matches.get(1).getUser().getFirst_name());
        assertEquals(true, matches.get(0).getScore() >= matches.get(1).getScore());
    }
}
