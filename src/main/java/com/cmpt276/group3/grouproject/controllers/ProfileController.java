package com.cmpt276.group3.grouproject.controllers;

import java.util.ArrayList;
import java.util.List;
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
        model.addAttribute("campuses", Campus.values());
        model.addAttribute("lifestyles", Lifestyle.values());
        model.addAttribute("topInterests", TopInterest.values());
        model.addAttribute("relationshipGoals", RelationshipGoal.values());
        model.addAttribute("relationshipPersonalities", RelationshipPersonality.values());
        model.addAttribute("relationshipCommunicationStyles", RelationshipCommunicationStyle.values());
        model.addAttribute("relationshipTextingStyles", RelationshipTextingStyle.values());
        model.addAttribute("relationshipFreeTimes", RelationshipFreeTime.values());
        model.addAttribute("relationshipValues", RelationshipValue.values());
        model.addAttribute("relationshipConflictStyles", RelationshipConflictStyle.values());
        model.addAttribute("relationshipLifestyles", RelationshipLifestyle.values());
        model.addAttribute("relationshipAmbitionImportances", RelationshipAmbitionImportance.values());
        model.addAttribute("relationshipCareStyles", RelationshipCareStyle.values());
        model.addAttribute("relationshipPersonalSpaces", RelationshipPersonalSpace.values());
        model.addAttribute("relationshipDateActivities", RelationshipDateActivity.values());
        model.addAttribute("relationshipSocialLives", RelationshipSocialLife.values());
        model.addAttribute("relationshipHumorStyles", RelationshipHumorStyle.values());
        model.addAttribute("relationshipStrengths", RelationshipStrength.values());
        model.addAttribute("sports", Sport.values());
        model.addAttribute("venues", Venue.values());
        model.addAttribute("hobbies", Hobby.values());
        model.addAttribute("genders", Gender.values());

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

        profile.setHas_job(parseBoolean(formData.get("has_job")));
        profile.setRegularly_goes_to_gym(parseBoolean(formData.get("regularly_goes_to_gym")));
        profile.setFavourite_sport(parseEnum(formData.get("favourite_sport"), Sport.class));
        profile.setPreferred_venue(parseEnum(formData.get("preferred_venue"), Venue.class));
        profile.setHobby1(parseEnum(formData.get("hobby1"), Hobby.class));
        profile.setHobby2(parseEnum(formData.get("hobby2"), Hobby.class));
        profile.setHobby3(parseEnum(formData.get("hobby3"), Hobby.class));
        profile.setHobby4(parseEnum(formData.get("hobby4"), Hobby.class));
        profile.setHobby5(parseEnum(formData.get("hobby5"), Hobby.class));

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
        profile.setCampus(parseEnum(formData.get("campus"), Campus.class));
        profile.setLifestyle(parseEnum(formData.get("lifestyle"), Lifestyle.class));
        profile.setMotivation(parseEnum(formData.get("motivation"), Motivation.class));
        profile.setFriend_type(parseEnum(formData.get("friend_type"), FriendType.class));
        profile.setTop_interests(parseEnum(formData.get("top_interests"), TopInterest.class));

        profile.setRelationship_goal(parseEnum(formData.get("relationship_goal"), RelationshipGoal.class));
        profile.setRelationship_personality(parseEnum(formData.get("relationship_personality"), RelationshipPersonality.class));
        profile.setRelationship_communication_style(parseEnum(formData.get("relationship_communication_style"), RelationshipCommunicationStyle.class));
        profile.setRelationship_texting_style(parseEnum(formData.get("relationship_texting_style"), RelationshipTextingStyle.class));
        profile.setRelationship_free_time(parseEnum(formData.get("relationship_free_time"), RelationshipFreeTime.class));
        profile.setRelationship_value(parseEnum(formData.get("relationship_value"), RelationshipValue.class));
        profile.setRelationship_conflict_style(parseEnum(formData.get("relationship_conflict_style"), RelationshipConflictStyle.class));
        profile.setRelationship_lifestyle(parseEnum(formData.get("relationship_lifestyle"), RelationshipLifestyle.class));
        profile.setRelationship_ambition_importance(parseEnum(formData.get("relationship_ambition_importance"), RelationshipAmbitionImportance.class));
        profile.setRelationship_care_style(parseEnum(formData.get("relationship_care_style"), RelationshipCareStyle.class));
        profile.setRelationship_personal_space(parseEnum(formData.get("relationship_personal_space"), RelationshipPersonalSpace.class));
        profile.setRelationship_date_activity(parseEnum(formData.get("relationship_date_activity"), RelationshipDateActivity.class));
        profile.setRelationship_social_life(parseEnum(formData.get("relationship_social_life"), RelationshipSocialLife.class));
        profile.setRelationship_humor_style(parseEnum(formData.get("relationship_humor_style"), RelationshipHumorStyle.class));
        profile.setRelationship_strength(parseEnum(formData.get("relationship_strength"), RelationshipStrength.class));

        profile.setLooking_for_short_term_relationship(parseBoolean(formData.get("looking_for_short_term_relationship")));
        profile.setMin_partner_age(parseInteger(formData.get("min_partner_age")));
        profile.setMax_partner_age(parseInteger(formData.get("max_partner_age")));
        profile.setPartner_gender(parseEnum(formData.get("partner_gender"), Gender.class));

        profile.setBuddy_area_of_study(parseEnum(formData.get("buddy_area_of_study"), StudyField.class));
        profile.setBuddy_min_year_of_study(parseInteger(formData.get("buddy_min_year_of_study")));
        profile.setBuddy_max_year_of_study(parseInteger(formData.get("buddy_max_year_of_study")));
        profile.setStudy_buddy_program(blankToNull(formData.get("study_buddy_program")));
        profile.setStudy_buddy_gender_preference(parseEnum(formData.get("study_buddy_gender_preference"), Gender.class));

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

        Integer friendshipMatchScore = null;
        Integer relationshipMatchScore = null;
        Integer studyBuddyMatchScore = null;

        if (!isOwnProfile && hasQuestionnaire && viewer != null) {
            MatchingProfile viewerProfile = matchingProfileService.getProfileByUser(viewer);

            if (viewerProfile != null) {
                if (matchingProfile.isDisplay_friendship_profile()
                        && viewerProfile.isDisplay_friendship_profile()) {
                    try {
                        friendshipMatchScore = MatchingAlgorithm.friendshipMatch(viewerProfile, matchingProfile);
                    } catch (RuntimeException e) {
                        friendshipMatchScore = null;
                    }
                }

                if (matchingProfile.isDisplay_dating_profile()
                        && viewerProfile.isDisplay_dating_profile()) {
                    try {
                        relationshipMatchScore = MatchingAlgorithm.relationshipMatch(viewerProfile, matchingProfile);
                    } catch (RuntimeException e) {
                        relationshipMatchScore = null;
                    }
                }

                if (matchingProfile.isDisplay_study_buddy_profile()
                        && viewerProfile.isDisplay_study_buddy_profile()) {
                    try {
                        studyBuddyMatchScore = MatchingAlgorithm.studyBuddyMatch(viewerProfile, matchingProfile);
                    } catch (RuntimeException e) {
                        studyBuddyMatchScore = null;
                    }
                }
            }
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("currentUser", viewer);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("matchingProfile", matchingProfile);
        model.addAttribute("hasQuestionnaire", hasQuestionnaire);
        model.addAttribute("friendshipMatchScore", friendshipMatchScore);
        model.addAttribute("relationshipMatchScore", relationshipMatchScore);
        model.addAttribute("studyBuddyMatchScore", studyBuddyMatchScore);
        model.addAttribute("studyBuddyCourses", parseStoredCourses(
            hasQuestionnaire ? matchingProfile.getStudy_buddy_courses() : null
        ));
        return "profile";
    }

    private List<String> parseStoredCourses(String storedCourses) {
        List<String> courses = new ArrayList<>();

        if (storedCourses == null || storedCourses.isBlank()) {
            return courses;
        }

        for (String course : storedCourses.split(",")) {
            if (course != null && !course.isBlank()) {
                courses.add(course.trim());
            }
        }

        return courses;
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

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        return null;
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
        return allPresent(formData,
            "age",
            "year_of_study",
            "study_field",
            "has_job",
            "regularly_goes_to_gym",
            "favourite_sport",
            "preferred_venue",
            "hobby1",
            "hobby2",
            "hobby3",
            "hobby4",
            "hobby5"
        );
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
            "relationship_strength",
            "looking_for_short_term_relationship",
            "min_partner_age",
            "max_partner_age",
            "partner_gender"
        ) && validPartnerAgeRange(formData);
    }

    private boolean validPartnerAgeRange(Map<String, String> formData) {
        Integer minAge = parseInteger(formData.get("min_partner_age"));
        Integer maxAge = parseInteger(formData.get("max_partner_age"));
        return minAge != null && maxAge != null && minAge >= 18 && maxAge >= minAge;
    }

    private boolean studyBuddyComplete(Map<String, String> formData, HttpServletRequest request) {
        if (!allPresent(formData,
                "buddy_area_of_study",
                "buddy_min_year_of_study",
                "buddy_max_year_of_study",
                "study_buddy_program",
                "study_buddy_gender_preference",
                "class_count")) {
            return false;
        }

        Integer classCount = parseInteger(formData.get("class_count"));
        if (classCount == null || classCount < 1 || classCount > 6) {
            return false;
        }

        Integer buddyMinYear = parseInteger(formData.get("buddy_min_year_of_study"));
        Integer buddyMaxYear = parseInteger(formData.get("buddy_max_year_of_study"));
        if (buddyMinYear == null || buddyMaxYear == null || buddyMinYear < 1 || buddyMaxYear < buddyMinYear) {
            return false;
        }

        String[] enteredCourses = request.getParameterValues("study_buddy_course");
        if (enteredCourses == null || enteredCourses.length != classCount) {
            return false;
        }

        java.util.HashSet<String> seenCourses = new java.util.HashSet<>();

        for (String course : enteredCourses) {
            if (course == null || course.isBlank()) {
                return false;
            }

            String normalizedCourse = course.trim().replaceAll("\\s+", " ").toUpperCase();
            if (seenCourses.contains(normalizedCourse)) {
                return false;
            }

            seenCourses.add(normalizedCourse);
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
