package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Availability;
import com.cmpt276.group3.grouproject.enums.Campus;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.enums.GroupSizePreference;
import com.cmpt276.group3.grouproject.enums.GroupVibe;
import com.cmpt276.group3.grouproject.enums.MeetupStyle;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.enums.TopInterest;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.models.GroupPreference;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.FriendGroupService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(FriendGroupController.class)
public class FriendGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Auth auth;

    @MockitoBean
    private FriendGroupService friendGroupService;

    private MockHttpSession session;
    private User mike;
    private FriendGroup group;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        mike = new User("Mike", "Chen", "mike@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        mike.setId(1L);

        group = new FriendGroup("Gaming Crew", "desc", GroupVibe.GAMING_NIGHTS, MeetupStyle.ON_CAMPUS,
                Campus.BURNABY, Availability.EVENINGS, TopInterest.GAMING_TECH,
                GroupSizePreference.SMALL, mike);
        group.setId(10L);
    }

    private void loggedIn() {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(mike);
    }

    private GroupPreference completePreference() {
        GroupPreference preference = new GroupPreference(mike);
        preference.setLookingForGroup(true);
        preference.setVibe(GroupVibe.GAMING_NIGHTS);
        preference.setSize_preference(GroupSizePreference.SMALL);
        preference.setMeetup_style(MeetupStyle.ON_CAMPUS);
        preference.setCampus(Campus.BURNABY);
        preference.setAvailability(Availability.EVENINGS);
        preference.setTop_interest(TopInterest.GAMING_TECH);
        return preference;
    }

    // --- Auth gate: every route must bounce a logged-out visitor ---

    @Test
    void allGroupPages_redirectToLoginWhenNotAuthenticated() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        String[] pages = {"/groups", "/groups/questionnaire", "/groups/find", "/groups/new", "/groups/10"};

        for (String page : pages) {
            mockMvc.perform(get(page).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void groupActions_redirectToLoginWhenNotAuthenticated() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        String[] actions = {"/groups/10/join", "/groups/10/request-join", "/groups/10/leave",
                "/groups/10/delete", "/groups/auto-match"};

        for (String action : actions) {
            mockMvc.perform(post(action).session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    // --- Landing page ---

    @Test
    void myGroups_rendersTheGroupsPage() throws Exception {
        loggedIn();
        when(friendGroupService.getPreference(mike)).thenReturn(completePreference());
        when(friendGroupService.getMyMemberships(mike)).thenReturn(new ArrayList<>(
                Arrays.asList(new GroupMembership(group, mike, GroupRole.ADMIN))));
        when(friendGroupService.getMemberCount(any(FriendGroup.class))).thenReturn(3L);

        mockMvc.perform(get("/groups").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("groups"))
                .andExpect(model().attributeExists("memberships", "memberCounts", "currentUser"))
                .andExpect(model().attribute("preferenceComplete", true));
    }

    @Test
    void myGroups_flagsAnIncompleteQuestionnaire() throws Exception {
        loggedIn();
        when(friendGroupService.getPreference(mike)).thenReturn(null);
        when(friendGroupService.getMyMemberships(mike)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/groups").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasPreference", false))
                .andExpect(model().attribute("preferenceComplete", false));
    }

    // --- Questionnaire ---

    @Test
    void questionnaire_rendersWithEveryDropdownPopulated() throws Exception {
        loggedIn();
        when(friendGroupService.getPreference(mike)).thenReturn(null);

        mockMvc.perform(get("/groups/questionnaire").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("groups-questionnaire"))
                .andExpect(model().attributeExists("vibes", "groupSizes", "meetupStyles",
                        "campuses", "availabilities", "lifestyles", "topInterests", "hobbies"));
    }

    @Test
    void saveQuestionnaire_storesTheAnswersAndSendsMikeToFind() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/questionnaire").session(session)
                        .param("looking_for_group", "on")
                        .param("vibe", "GAMING_NIGHTS")
                        .param("size_preference", "SMALL")
                        .param("meetup_style", "ON_CAMPUS")
                        .param("campus", "BURNABY")
                        .param("availability", "EVENINGS")
                        .param("lifestyle", "BALANCED")
                        .param("top_interest", "GAMING_TECH")
                        .param("hobby1", "VIDEO_GAMES")
                        .param("blurb", "third year CS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/find*"));

        verify(friendGroupService).savePreference(eq(mike), any(GroupPreference.class));
    }

    // An unchecked box submits nothing at all, which has to mean "not looking".
    @Test
    void saveQuestionnaire_treatsAMissingCheckboxAsNotLooking() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/questionnaire").session(session)
                        .param("vibe", "GAMING_NIGHTS")
                        .param("size_preference", "SMALL"))
                .andExpect(status().is3xxRedirection());

        verify(friendGroupService).savePreference(eq(mike),
                argThat((GroupPreference preference) -> !preference.isLookingForGroup()));
    }

    // A junk enum value should be dropped, not blow up the request.
    @Test
    void saveQuestionnaire_ignoresUnknownDropdownValues() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/questionnaire").session(session)
                        .param("vibe", "NOT_A_REAL_VIBE")
                        .param("campus", ""))
                .andExpect(status().is3xxRedirection());

        verify(friendGroupService).savePreference(eq(mike),
                argThat((GroupPreference preference) ->
                        preference.getVibe() == null && preference.getCampus() == null));
    }

    // --- Find ---

    @Test
    void find_showsTheQuestionnairePromptWhenAnswersAreMissing() throws Exception {
        loggedIn();
        when(friendGroupService.getPreference(mike)).thenReturn(null);

        mockMvc.perform(get("/groups/find").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("groups-find"))
                .andExpect(model().attribute("preferenceComplete", false));

        // No point scoring groups for someone we can't match yet.
        verify(friendGroupService, never()).suggestGroupsFor(any(User.class));
    }

    @Test
    void find_listsScoredSuggestionsOnceTheQuestionnaireIsDone() throws Exception {
        loggedIn();
        when(friendGroupService.getPreference(mike)).thenReturn(completePreference());
        when(friendGroupService.suggestGroupsFor(mike)).thenReturn(new ArrayList<>(
                Arrays.asList(new FriendGroupService.GroupSuggestion(group, 82, 2))));

        mockMvc.perform(get("/groups/find").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("preferenceComplete", true))
                .andExpect(model().attributeExists("suggestions"));

        verify(friendGroupService).suggestGroupsFor(mike);
    }

    // --- Auto-match ---

    @Test
    void autoMatch_sendsMikeToTheNewGroupOnSuccess() throws Exception {
        loggedIn();
        when(friendGroupService.autoMatchIntoGroup(mike)).thenReturn(group);

        mockMvc.perform(post("/groups/auto-match").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10*"));
    }

    @Test
    void autoMatch_explainsItselfWhenThereArentEnoughStudents() throws Exception {
        loggedIn();
        when(friendGroupService.autoMatchIntoGroup(mike)).thenReturn(null);

        mockMvc.perform(post("/groups/auto-match").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/find?error=*"));
    }

    @Test
    void autoMatch_surfacesServiceErrorsInsteadOfThrowing() throws Exception {
        loggedIn();
        when(friendGroupService.autoMatchIntoGroup(mike))
                .thenThrow(new IllegalStateException("Complete the group finder questionnaire first"));

        mockMvc.perform(post("/groups/auto-match").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/find?error=*"));
    }

    // --- Group detail ---

    @Test
    void viewGroup_rendersTheGroupPage() throws Exception {
        loggedIn();
        when(friendGroupService.findGroup(10L)).thenReturn(group);
        when(friendGroupService.getMemberships(group)).thenReturn(new ArrayList<>(
                Arrays.asList(new GroupMembership(group, mike, GroupRole.ADMIN))));
        when(friendGroupService.isMember(group, mike)).thenReturn(true);
        when(friendGroupService.isAdmin(group, mike)).thenReturn(true);

        mockMvc.perform(get("/groups/10").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("group"))
                .andExpect(model().attribute("isGroupAdmin", true))
                .andExpect(model().attribute("isMember", true))
                .andExpect(model().attribute("isApprovedMember", true))
                .andExpect(model().attribute("memberCount", 1));
    }

    @Test
    void viewGroup_allowsASitewideModeratorWhoIsNotAMember() throws Exception {
        User moderator = new User("Mod", "User", "mod@sfu.ca", "pw", Role.MOD, Gender.NONE, "");
        moderator.setId(99L);
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(moderator);
        when(friendGroupService.findGroup(10L)).thenReturn(group);
        when(friendGroupService.getMemberships(group)).thenReturn(new ArrayList<>(
                Arrays.asList(new GroupMembership(group, mike, GroupRole.ADMIN))));
        when(friendGroupService.isMember(group, moderator)).thenReturn(false);

        mockMvc.perform(get("/groups/10").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("group"))
                .andExpect(model().attribute("isGroupAdmin", true))
                .andExpect(model().attribute("isMember", false));
    }

    @Test
    void viewGroup_redirectsWhenTheGroupIsGone() throws Exception {
        loggedIn();
        when(friendGroupService.findGroup(999L)).thenReturn(null);

        mockMvc.perform(get("/groups/999").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups?error=*"));
    }

    // --- Membership actions ---

    @Test
    void join_returnsToTheGroupPageOnSuccess() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/join").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10?success=*"));

        verify(friendGroupService).joinGroup(mike, 10L);
    }

    @Test
    void join_showsTheReasonWhenTheGroupIsFull() throws Exception {
        loggedIn();
        doThrow(new IllegalStateException("This group is full"))
                .when(friendGroupService).joinGroup(mike, 10L);

        mockMvc.perform(post("/groups/10/join").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10?error=*"));
    }

    @Test
    void requestToJoin_returnsToTheExploreListOnSuccess() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/request-join").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/find?success=*"));

        verify(friendGroupService).requestToJoinGroup(mike, 10L);
    }

    @Test
    void leave_sendsMikeBackToHisGroupList() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/leave").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups?success=*"));

        verify(friendGroupService).leaveGroup(mike, 10L);
    }

    @Test
    void delete_sendsMikeBackToHisGroupList() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups?success=*"));

        verify(friendGroupService).deleteGroup(mike, 10L);
    }

    @Test
    void delete_surfacesThePermissionErrorForANonAdmin() throws Exception {
        loggedIn();
        doThrow(new IllegalStateException("Only a group admin can do that"))
                .when(friendGroupService).deleteGroup(mike, 10L);

        mockMvc.perform(post("/groups/10/delete").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10?error=*"));
    }

    @Test
    void changeRole_passesTheRequestedRoleThrough() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/members/7/role").session(session).param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10?success=*"));

        verify(friendGroupService).changeRole(mike, 10L, 7L, GroupRole.ADMIN);
    }

    @Test
    void changeRole_rejectsAnUnknownRoleWithoutCallingTheService() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/members/7/role").session(session).param("role", "SUPERUSER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10?error=*"));

        verify(friendGroupService, never()).changeRole(any(User.class), anyLong(), anyLong(), any(GroupRole.class));
    }

    @Test
    void removeMember_returnsToTheGroupPage() throws Exception {
        loggedIn();

        mockMvc.perform(post("/groups/10/members/7/remove").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10?success=*"));

        verify(friendGroupService).removeMember(mike, 10L, 7L);
    }

    // --- Creating a group ---

    @Test
    void createGroup_sendsMikeToTheNewGroup() throws Exception {
        loggedIn();
        when(friendGroupService.createGroup(eq(mike), any(FriendGroup.class))).thenReturn(group);

        mockMvc.perform(post("/groups/new").session(session)
                        .param("name", "Gaming Crew")
                        .param("description", "we play games")
                        .param("vibe", "GAMING_NIGHTS")
                        .param("size_preference", "SMALL")
                        .param("meetup_style", "ON_CAMPUS")
                        .param("campus", "BURNABY")
                        .param("availability", "EVENINGS")
                        .param("top_interest", "GAMING_TECH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/10*"));
    }

    @Test
    void createGroup_returnsToTheFormWhenTheServiceRejectsIt() throws Exception {
        loggedIn();
        when(friendGroupService.createGroup(eq(mike), any(FriendGroup.class)))
                .thenThrow(new IllegalArgumentException("A group name is required"));

        mockMvc.perform(post("/groups/new").session(session).param("name", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/groups/new?error=*"));
    }
}
