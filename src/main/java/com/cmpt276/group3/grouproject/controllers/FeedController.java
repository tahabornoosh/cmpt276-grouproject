package com.cmpt276.group3.grouproject.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cmpt276.group3.grouproject.algorithms.MatchingAlgorithm;
import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.EOIStream;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;

import jakarta.servlet.http.HttpSession;

// Case: Using Feeds
// GET /feeds/{streamSlug} where streamSlug is "friendship", "relationship", or "study-buddy".
// Reached from the "Feeds" item + sub-menu added to the sidebar in fragments/layout.html.
@Controller
public class FeedController {

    private final Auth auth;
    private final MatchingProfileService matchingProfileService;
    private final MatchingProfileRepository matchingProfileRepository;

    public FeedController(Auth auth, MatchingProfileService matchingProfileService,
            MatchingProfileRepository matchingProfileRepository) {
        this.auth = auth;
        this.matchingProfileService = matchingProfileService;
        this.matchingProfileRepository = matchingProfileRepository;
    }

    @GetMapping("/feeds/{streamSlug}")
    public String viewFeed(@PathVariable String streamSlug, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);
        EOIStream stream = parseStreamSlug(streamSlug);

        if (currentUser == null || stream == null) {
            return "redirect:/";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("stream", stream);
        model.addAttribute("streamSlug", streamSlug);
        model.addAttribute("badgeClass", badgeClassFor(stream));
        model.addAttribute("streamIcon", iconFor(stream));

        MatchingProfile viewerProfile = matchingProfileService.getProfileByUser(currentUser);

        if (viewerProfile == null || !streamEnabled(viewerProfile, stream)) {
            // Acceptance criteria: an informative error + link to the questionnaire,
            // not a broken/empty feed, when Mike hasn't completed & enabled this stream.
            model.addAttribute("streamIncomplete", true);
            return "feed";
        }

        model.addAttribute("streamIncomplete", false);
        model.addAttribute("matches", buildFeed(currentUser, viewerProfile, stream));
        return "feed";
    }

    private List<FeedEntry> buildFeed(User currentUser, MatchingProfile viewerProfile, EOIStream stream) {
        List<FeedEntry> entries = new ArrayList<>();

        for (MatchingProfile candidate : candidatesForStream(stream)) {
            if (candidate.getUser().getId() == currentUser.getId()) {
                continue; // don't match Mike with himself
            }

            Integer score = scoreFor(stream, viewerProfile, candidate);
            if (score == null || score < 60) {
                // -1 means an exclusionary question failed (e.g. incompatible relationship goals,
                // age range, or gender preference) - null means the algorithm couldn't be run at
                // all (e.g. a required field was left blank). Either way, leave it out of the feed.
                // scores less than 60 excluded
                continue;
            }

            entries.add(new FeedEntry(candidate.getUser(), score));
        }

        // Acceptance criteria: ordered highest score to lowest.
        entries.sort(Comparator.comparingInt(FeedEntry::getScore).reversed());
        return entries;
    }

    private List<MatchingProfile> candidatesForStream(EOIStream stream) {
        List<MatchingProfile> candidates = new ArrayList<>();

        for (MatchingProfile profile : matchingProfileRepository.findAll()) {
            if (streamEnabled(profile, stream)) {
                candidates.add(profile);
            }
        }

        return candidates;
    }

    private Integer scoreFor(EOIStream stream, MatchingProfile viewerProfile, MatchingProfile candidate) {
        try {
            switch (stream) {
                case FRIENDSHIP:
                    return MatchingAlgorithm.friendshipMatch(viewerProfile, candidate);
                case RELATIONSHIP:
                    return MatchingAlgorithm.relationshipMatch(viewerProfile, candidate);
                case STUDY_BUDDY:
                    return MatchingAlgorithm.studyBuddyMatch(viewerProfile, candidate);
                default:
                    return null;
            }
        } catch (RuntimeException e) {
            // A profile missing a field the algorithm needs shouldn't take down the whole feed.
            return null;
        }
    }

    private boolean streamEnabled(MatchingProfile profile, EOIStream stream) {
        switch (stream) {
            case FRIENDSHIP:
                return profile.isDisplay_friendship_profile();
            case RELATIONSHIP:
                return profile.isDisplay_dating_profile();
            case STUDY_BUDDY:
                return profile.isDisplay_study_buddy_profile();
            default:
                return false;
        }
    }

    private String badgeClassFor(EOIStream stream) {
        switch (stream) {
            case FRIENDSHIP:
                return "badge-info";
            case RELATIONSHIP:
                return "badge-danger";
            case STUDY_BUDDY:
                return "badge-success";
            default:
                return "badge-secondary";
        }
    }

    private String iconFor(EOIStream stream) {
        switch (stream) {
            case FRIENDSHIP:
                return "fa-user-friends";
            case RELATIONSHIP:
                return "fa-heart";
            case STUDY_BUDDY:
                return "fa-book-reader";
            default:
                return "fa-users";
        }
    }

    private EOIStream parseStreamSlug(String slug) {
        if (slug == null) {
            return null;
        }
        switch (slug.toLowerCase()) {
            case "friendship":
                return EOIStream.FRIENDSHIP;
            case "relationship":
                return EOIStream.RELATIONSHIP;
            case "study-buddy":
            case "study_buddy":
                return EOIStream.STUDY_BUDDY;
            default:
                return null;
        }
    }

    // Small view-model pairing a candidate's User with their score for this stream,
    // so the template can render name/avatar/score/button without touching MatchingProfile directly.
    public static class FeedEntry {
        private final User user;
        private final int score;

        public FeedEntry(User user, int score) {
            this.user = user;
            this.score = score;
        }

        public User getUser() {
            return user;
        }

        public int getScore() {
            return score;
        }
    }
}
