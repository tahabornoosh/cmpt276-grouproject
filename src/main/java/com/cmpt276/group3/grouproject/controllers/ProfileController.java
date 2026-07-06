package com.cmpt276.group3.grouproject.controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cmpt276.group3.grouproject.algorithms.MatchingAlgorithm;
import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;

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

    // Public within the app: any logged-in user can view any other user's public profile.
    // The MatchingProfile (questionnaire) is a separate, optional entity - a user may not
    // have filled one out yet, so we treat it as absent rather than error out.
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
                    // Zero hobby overlap sends the algorithm's internal log() to -Infinity,
                    // which isn't an exception but does produce a nonsensical negative score -
                    // clamp it for display rather than changing the scoring formula itself.
                    if (matchScore < 0) {
                        matchScore = 0;
                    }
                } catch (RuntimeException e) {
                    // Either profile is still missing some of the fields the algorithm
                    // relies on (e.g. age) - fall back to no score instead of a 500.
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
}

