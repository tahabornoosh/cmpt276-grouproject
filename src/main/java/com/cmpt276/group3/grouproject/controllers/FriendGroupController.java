package com.cmpt276.group3.grouproject.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Availability;
import com.cmpt276.group3.grouproject.enums.Campus;
import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.enums.GroupSizePreference;
import com.cmpt276.group3.grouproject.enums.GroupVibe;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.Lifestyle;
import com.cmpt276.group3.grouproject.enums.MeetupStyle;
import com.cmpt276.group3.grouproject.enums.TopInterest;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.models.GroupPreference;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.FriendGroupService;

import jakarta.servlet.http.HttpSession;

// Case: Friend Group Finder (task 5 - group construction)
// Reached from the "Groups" item in the sidebar in fragments/layout.html.
//
// Every handler follows the same shape as the existing controllers: check the session,
// pull the current user, add "currentUser" to the model for the layout fragment, and
// redirect back with an ?error= / ?success= query param instead of throwing to the user.
@Controller
public class FriendGroupController {

    private final Auth auth;
    private final FriendGroupService friendGroupService;

    public FriendGroupController(Auth auth, FriendGroupService friendGroupService) {
        this.auth = auth;
        this.friendGroupService = friendGroupService;
    }

    // ----- Landing page: the groups I'm in -----

    @GetMapping("/groups")
    public String myGroups(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        GroupPreference preference = friendGroupService.getPreference(currentUser);
        List<GroupMembership> memberships = friendGroupService.getMyMemberships(currentUser);

        // Headcounts for each of my groups, in the same order as the membership list,
        // so the template can show "4 / 6 members" without hitting the service itself.
        List<Long> memberCounts = new ArrayList<>();
        for (GroupMembership membership : memberships) {
            memberCounts.add(friendGroupService.getMemberCount(membership.getGroup()));
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("memberships", memberships);
        model.addAttribute("memberCounts", memberCounts);
        model.addAttribute("preference", preference);
        model.addAttribute("hasPreference", preference != null);
        model.addAttribute("preferenceComplete", preference != null && preference.isComplete());

        return "groups";
    }

    // ----- The group finder questionnaire -----

    @GetMapping("/groups/questionnaire")
    public String questionnaire(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        GroupPreference preference = friendGroupService.getPreference(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("preference", preference);
        model.addAttribute("hasPreference", preference != null);
        addEnumOptions(model);

        return "groups-questionnaire";
    }

    @PostMapping("/groups/questionnaire")
    public String saveQuestionnaire(
            HttpSession session,
            @RequestParam(value = "looking_for_group", required = false) String lookingForGroup,
            @RequestParam(value = "vibe", required = false) String vibe,
            @RequestParam(value = "size_preference", required = false) String sizePreference,
            @RequestParam(value = "meetup_style", required = false) String meetupStyle,
            @RequestParam(value = "campus", required = false) String campus,
            @RequestParam(value = "availability", required = false) String availability,
            @RequestParam(value = "lifestyle", required = false) String lifestyle,
            @RequestParam(value = "top_interest", required = false) String topInterest,
            @RequestParam(value = "hobby1", required = false) String hobby1,
            @RequestParam(value = "hobby2", required = false) String hobby2,
            @RequestParam(value = "hobby3", required = false) String hobby3,
            @RequestParam(value = "blurb", required = false) String blurb) {

        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        GroupPreference submitted = new GroupPreference(currentUser);
        submitted.setLookingForGroup(lookingForGroup != null);
        submitted.setVibe(parseEnum(GroupVibe.class, vibe));
        submitted.setSize_preference(parseEnum(GroupSizePreference.class, sizePreference));
        submitted.setMeetup_style(parseEnum(MeetupStyle.class, meetupStyle));
        submitted.setCampus(parseEnum(Campus.class, campus));
        submitted.setAvailability(parseEnum(Availability.class, availability));
        submitted.setLifestyle(parseEnum(Lifestyle.class, lifestyle));
        submitted.setTop_interest(parseEnum(TopInterest.class, topInterest));
        submitted.setHobby1(parseEnum(Hobby.class, hobby1));
        submitted.setHobby2(parseEnum(Hobby.class, hobby2));
        submitted.setHobby3(parseEnum(Hobby.class, hobby3));
        submitted.setBlurb(blurb);

        try {
            friendGroupService.savePreference(currentUser, submitted);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/questionnaire?error=" + encode(e.getMessage());
        }

        return "redirect:/groups/find?success=" + encode("Preferences saved");
    }

    // ----- Finding a group -----

    @GetMapping("/groups/find")
    public String findGroups(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        GroupPreference preference = friendGroupService.getPreference(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("preference", preference);
        model.addAttribute("hasPreference", preference != null);
        model.addAttribute("preferenceComplete", preference != null && preference.isComplete());

        // Mirrors the feed page: rather than showing an empty list, tell the user
        // exactly what they still need to do and link them to the questionnaire.
        if (preference == null || !preference.isComplete()) {
            model.addAttribute("suggestions", new ArrayList<>());
            return "groups-find";
        }

        model.addAttribute("suggestions", friendGroupService.suggestGroupsFor(currentUser));
        return "groups-find";
    }

    @PostMapping("/groups/auto-match")
    public String autoMatch(HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        FriendGroup created;

        try {
            created = friendGroupService.autoMatchIntoGroup(currentUser);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/find?error=" + encode(e.getMessage());
        }

        if (created == null) {
            return "redirect:/groups/find?error="
                    + encode("Not enough compatible students are looking for a group right now. "
                            + "Try again later or join an existing group below.");
        }

        return "redirect:/groups/" + created.getId() + "?success=" + encode("Your new group is ready");
    }

    // ----- Creating a group by hand -----

    @GetMapping("/groups/new")
    public String newGroupForm(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        addEnumOptions(model);

        return "groups-new";
    }

    @PostMapping("/groups/new")
    public String createGroup(
            HttpSession session,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "vibe", required = false) String vibe,
            @RequestParam(value = "meetup_style", required = false) String meetupStyle,
            @RequestParam(value = "campus", required = false) String campus,
            @RequestParam(value = "availability", required = false) String availability,
            @RequestParam(value = "top_interest", required = false) String topInterest,
            @RequestParam(value = "size_preference", required = false) String sizePreference) {

        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        FriendGroup form = new FriendGroup(
                name,
                description,
                parseEnum(GroupVibe.class, vibe),
                parseEnum(MeetupStyle.class, meetupStyle),
                parseEnum(Campus.class, campus),
                parseEnum(Availability.class, availability),
                parseEnum(TopInterest.class, topInterest),
                parseEnum(GroupSizePreference.class, sizePreference),
                currentUser);

        FriendGroup created;

        try {
            created = friendGroupService.createGroup(currentUser, form);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/new?error=" + encode(e.getMessage());
        }

        return "redirect:/groups/" + created.getId() + "?success=" + encode("Group created");
    }

    // ----- A single group -----

    @GetMapping("/groups/{id}")
    public String viewGroup(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        FriendGroup group = friendGroupService.findGroup(id);

        if (group == null) {
            return "redirect:/groups?error=" + encode("Group not found");
        }

        List<GroupMembership> memberships = friendGroupService.getMemberships(group);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("group", group);
        model.addAttribute("memberships", memberships);
        model.addAttribute("memberCount", memberships.size());
        model.addAttribute("isMember", friendGroupService.isMember(group, currentUser));
        model.addAttribute("isGroupAdmin", friendGroupService.isAdmin(group, currentUser));
        model.addAttribute("isFull", memberships.size() >= group.getMaxMembers());

        return "group";
    }

    @PostMapping("/groups/{id}/edit")
    public String editGroup(
            @PathVariable("id") Long id,
            HttpSession session,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "open_to_new_members", required = false) String openToNewMembers) {

        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            friendGroupService.updateGroup(currentUser, id, name, description, openToNewMembers != null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/" + id + "?error=" + encode(e.getMessage());
        }

        return "redirect:/groups/" + id + "?success=" + encode("Group updated");
    }

    @PostMapping("/groups/{id}/delete")
    public String deleteGroup(@PathVariable("id") Long id, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            friendGroupService.deleteGroup(currentUser, id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/" + id + "?error=" + encode(e.getMessage());
        }

        return "redirect:/groups?success=" + encode("Group deleted");
    }

    @PostMapping("/groups/{id}/join")
    public String joinGroup(@PathVariable("id") Long id, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            friendGroupService.joinGroup(currentUser, id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/" + id + "?error=" + encode(e.getMessage());
        }

        return "redirect:/groups/" + id + "?success=" + encode("You joined the group");
    }

    @PostMapping("/groups/{id}/leave")
    public String leaveGroup(@PathVariable("id") Long id, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            friendGroupService.leaveGroup(currentUser, id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/" + id + "?error=" + encode(e.getMessage());
        }

        return "redirect:/groups?success=" + encode("You left the group");
    }

    @PostMapping("/groups/{id}/members/{userId}/role")
    public String changeRole(
            @PathVariable("id") Long id,
            @PathVariable("userId") long userId,
            HttpSession session,
            @RequestParam("role") String role) {

        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        GroupRole newRole = parseEnum(GroupRole.class, role);

        if (newRole == null) {
            return "redirect:/groups/" + id + "?error=" + encode("Unknown role");
        }

        try {
            friendGroupService.changeRole(currentUser, id, userId, newRole);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/" + id + "?error=" + encode(e.getMessage());
        }

        return "redirect:/groups/" + id + "?success=" + encode("Role updated");
    }

    @PostMapping("/groups/{id}/members/{userId}/remove")
    public String removeMember(
            @PathVariable("id") Long id,
            @PathVariable("userId") long userId,
            HttpSession session) {

        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            friendGroupService.removeMember(currentUser, id, userId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/groups/" + id + "?error=" + encode(e.getMessage());
        }

        return "redirect:/groups/" + id + "?success=" + encode("Member removed");
    }

    // ----- Helpers -----

    // Every dropdown on the group pages is populated from these.
    private void addEnumOptions(Model model) {
        model.addAttribute("vibes", GroupVibe.values());
        model.addAttribute("groupSizes", GroupSizePreference.values());
        model.addAttribute("meetupStyles", MeetupStyle.values());
        model.addAttribute("campuses", Campus.values());
        model.addAttribute("availabilities", Availability.values());
        model.addAttribute("lifestyles", Lifestyle.values());
        model.addAttribute("topInterests", TopInterest.values());
        model.addAttribute("hobbies", Hobby.values());
    }

    // Blank or unrecognised form values become null rather than blowing up the request.
    private static <T extends Enum<T>> T parseEnum(Class<T> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Messages are round-tripped through the query string, so they need escaping.
    private static String encode(String message) {
        if (message == null) {
            return "";
        }
        return URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
