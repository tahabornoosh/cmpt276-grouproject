package com.cmpt276.group3.grouproject.controllers;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmpt276.group3.grouproject.algorithms.MatchingAlgorithm;
import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.*;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {
    private final Auth auth;
    private final UsersRepository usersRepository;
    private final MatchingProfileService matchingProfileService;

    public ProfileController(Auth auth, UsersRepository usersRepository, MatchingProfileService matchingProfileService) {
        this.auth = auth;
        this.usersRepository = usersRepository;
        this.matchingProfileService = matchingProfileService;
    }

    @GetMapping("/questionnaire")
    public String questionnaire(@RequestParam(value = "edit", required = false) String edit, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);
        MatchingProfile matchingProfile = matchingProfileService.getProfileByUser(currentUser);

        if (matchingProfile != null && edit == null) {
            return "redirect:/profile/" + currentUser.getId();
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("matchingProfile", matchingProfile);
        model.addAttribute("hasQuestionnaire", matchingProfile != null);

        model.addAttribute("studyFields", StudyField.values());
        model.addAttribute("friendshipStyles", FriendshipStyle.values());
        model.addAttribute("socialStyles", SocialStyle.values());
        model.addAttribute("hangoutFrequencies", HangoutFrequency.values());
        model.addAttribute("friendActivities", FriendActivity.values());
        model.addAttribute("planningStyles", PlanningStyle.values());
        model.addAttribute("conversationStyles", ConversationStyle.values());
        model.addAttribute("communicationStyles", CommunicationStyle.values());
        model.addAttribute("personalityTraits", PersonalityTrait.values());
        model.addAttribute("friendshipValues", FriendshipValue.values());
        model.addAttribute("availabilities", Availability.values());
        model.addAttribute("motivations", Motivation.values());
        model.addAttribute("friendTypes", FriendType.values());

        return "questionnaire";
    }

    @PostMapping("/questionnaire")
    public String saveQuestionnaire(@RequestParam Map<String, String> formData, HttpServletRequest request, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!basicProfileComplete(formData)) {
            return "redirect:/questionnaire?edit=1&basicIncomplete=1";
        }

        if (formData.containsKey("display_friendship_profile") && !friendshipComplete(formData)) {
            return "redirect:/questionnaire?edit=1&friendshipIncomplete=1";
        }

        if (formData.containsKey("display_dating_profile") && !relationshipComplete(formData)) {
            return "redirect:/questionnaire?edit=1&relationshipIncomplete=1";
        }

        if (formData.containsKey("display_study_buddy_profile") && !studyBuddyComplete(formData, request)) {
            return "redirect:/questionnaire?edit=1&studyBuddyIncomplete=1";
        }

        MatchingProfile profile = new MatchingProfile();
        profile.setUser(currentUser);

        profile.setDisplay_friendship_profile(formData.containsKey("display_friendship_profile"));
        profile.setDisplay_dating_profile(formData.containsKey("display_dating_profile"));
        profile.setDisplay_study_buddy_profile(formData.containsKey("display_study_buddy_profile"));

        profile.setAge(parseInteger(formData.get("age")));
        profile.setStudy_field(parseEnum(formData.get("study_field"), StudyField.class));

        Integer yearOfStudy = parseInteger(formData.get("year_of_study"));
        if (yearOfStudy != null) {
            profile.setYear_of_study(yearOfStudy);
        }

        profile.setKind_of_friendship(parseEnum(formData.get("kind_of_friendship"), FriendshipStyle.class));
        profile.setSocial_style(parseEnum(formData.get("social_style"), SocialStyle.class));
        profile.setHangout_frequency(parseEnum(formData.get("hangout_frequency"), HangoutFrequency.class));
        profile.setFriend_activity(parseEnum(formData.get("friend_activity"), FriendActivity.class));
        profile.setPlanning_style(parseEnum(formData.get("planning_style"), PlanningStyle.class));
        profile.setConversation_style(parseEnum(formData.get("conversation_style"), ConversationStyle.class));
        profile.setCommunication_style(parseEnum(formData.get("communication_style"), CommunicationStyle.class));
        profile.setPersonality_trait(parseEnum(formData.get("personality_trait"), PersonalityTrait.class));
        profile.setFriendship_value(parseEnum(formData.get("friendship_value"), FriendshipValue.class));
        profile.setAvailability(parseEnum(formData.get("availability"), Availability.class));
        profile.setCampus(blankToNull(formData.get("campus")));
        profile.setLifestyle(blankToNull(formData.get("lifestyle")));
        profile.setMotivation(parseEnum(formData.get("motivation"), Motivation.class));
        profile.setFriend_type(parseEnum(formData.get("friend_type"), FriendType.class));
        profile.setTop_interests(blankToNull(formData.get("top_interests")));

        profile.setRelationship_goal(blankToNull(formData.get("relationship_goal")));
        profile.setRelationship_personality(blankToNull(formData.get("relationship_personality")));
        profile.setRelationship_communication_style(blankToNull(formData.get("relationship_communication_style")));
        profile.setRelationship_texting_style(blankToNull(formData.get("relationship_texting_style")));
        profile.setRelationship_free_time(blankToNull(formData.get("relationship_free_time")));
        profile.setRelationship_value(blankToNull(formData.get("relationship_value")));
        profile.setRelationship_conflict_style(blankToNull(formData.get("relationship_conflict_style")));
        profile.setRelationship_lifestyle(blankToNull(formData.get("relationship_lifestyle")));
        profile.setRelationship_ambition_importance(blankToNull(formData.get("relationship_ambition_importance")));
        profile.setRelationship_care_style(blankToNull(formData.get("relationship_care_style")));
        profile.setRelationship_personal_space(blankToNull(formData.get("relationship_personal_space")));
        profile.setRelationship_date_activity(blankToNull(formData.get("relationship_date_activity")));
        profile.setRelationship_social_life(blankToNull(formData.get("relationship_social_life")));
        profile.setRelationship_humor_style(blankToNull(formData.get("relationship_humor_style")));
        profile.setRelationship_strength(blankToNull(formData.get("relationship_strength")));

        profile.setLooking_for_short_term_relationship("CASUAL_DATING".equals(formData.get("relationship_goal")));
        profile.setMin_partner_age(18);
        profile.setMax_partner_age(99);

        profile.setStudy_buddy_program(blankToNull(formData.get("study_buddy_program")));
        profile.setStudy_buddy_gender_preference(blankToNull(formData.get("study_buddy_gender_preference")));

        String[] enteredCourses = request.getParameterValues("study_buddy_course");
        if (enteredCourses != null) {
            StringBuilder courses = new StringBuilder();
            for (String course : enteredCourses) {
                if (course != null && !course.isBlank()) {
                    if (courses.length() > 0) {
                        courses.append(", ");
                    }
                    courses.append(course.trim());
                }
            }
            profile.setStudy_buddy_courses(courses.length() > 0 ? courses.toString() : null);
        } else {
            profile.setStudy_buddy_courses(null);
        }

        try {
            if (matchingProfileService.profileForUserExists(currentUser)) {
                matchingProfileService.updateProfile(profile);
            } else {
                matchingProfileService.registerProfile(profile);
            }

            return "redirect:/profile/" + currentUser.getId() + "?questionnaireSaved=1";
        } catch (Exception e) {
            return "redirect:/questionnaire?error=1";
        }
    }

    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<User> profileUserOpt = usersRepository.findById(id);
        if (profileUserOpt.isEmpty()) {
            return "redirect:/";
        }
        User profileUser = profileUserOpt.get();

        User viewer = auth.getUser(session);
        boolean isOwnProfile = viewer != null && viewer.getId() == profileUser.getId();

        MatchingProfile matchingProfile = matchingProfileService.getProfileByUser(profileUser);
        boolean hasQuestionnaire = matchingProfile != null;

        Integer matchScore = null;
        if (!isOwnProfile && hasQuestionnaire && matchingProfile.isDisplay_friendship_profile() && viewer != null) {
            MatchingProfile viewerProfile = matchingProfileService.getProfileByUser(viewer);
            if (viewerProfile != null) {
                try {
                    matchScore = MatchingAlgorithm.friendshipMatch(viewerProfile, matchingProfile);
                    if (matchScore < 0) {
                        matchScore = 0;
                    }
                } catch (RuntimeException e) {
                    matchScore = null;
                }
            }
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("currentUser", viewer);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("matchingProfile", matchingProfile);
        model.addAttribute("hasQuestionnaire", hasQuestionnaire);
        model.addAttribute("matchScore", matchScore);
        return "profile";
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumType) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean basicProfileComplete(Map<String, String> formData) {
        return allPresent(formData, "age", "year_of_study", "study_field");
    }

    private boolean friendshipComplete(Map<String, String> formData) {
        return allPresent(formData,
            "kind_of_friendship",
            "social_style",
            "hangout_frequency",
            "friend_activity",
            "planning_style",
            "conversation_style",
            "communication_style",
            "personality_trait",
            "friendship_value",
            "availability",
            "campus",
            "lifestyle",
            "motivation",
            "friend_type",
            "top_interests"
        );
    }

    private boolean relationshipComplete(Map<String, String> formData) {
        return allPresent(formData,
            "relationship_goal",
            "relationship_personality",
            "relationship_communication_style",
            "relationship_texting_style",
            "relationship_free_time",
            "relationship_value",
            "relationship_conflict_style",
            "relationship_lifestyle",
            "relationship_ambition_importance",
            "relationship_care_style",
            "relationship_personal_space",
            "relationship_date_activity",
            "relationship_social_life",
            "relationship_humor_style",
            "relationship_strength"
        );
    }

    private boolean studyBuddyComplete(Map<String, String> formData, HttpServletRequest request) {
        if (!allPresent(formData, "study_buddy_program", "study_buddy_gender_preference", "class_count")) {
            return false;
        }

        Integer classCount = parseInteger(formData.get("class_count"));
        if (classCount == null || classCount < 1 || classCount > 6) {
            return false;
        }

        String[] enteredCourses = request.getParameterValues("study_buddy_course");
        if (enteredCourses == null || enteredCourses.length != classCount) {
            return false;
        }

        for (String course : enteredCourses) {
            if (course == null || course.isBlank()) {
                return false;
            }
        }

        return true;
    }

    private boolean allPresent(Map<String, String> formData, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = formData.get(fieldName);
            if (value == null || value.isBlank()) {
                return false;
            }
        }
        return true;
    }

}
