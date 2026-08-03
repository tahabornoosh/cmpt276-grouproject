package com.cmpt276.group3.grouproject.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.ChatMessage;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterest;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterestRepository;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.FriendGroupRepository;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.transaction.annotation.Transactional;

import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.models.CourseRatingRepository;
import com.cmpt276.group3.grouproject.models.GroupChatMessage;
import com.cmpt276.group3.grouproject.models.GroupChatMessageRepository;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.models.GroupMembershipRepository;
import com.cmpt276.group3.grouproject.models.GroupPreferenceRepository;
import com.cmpt276.group3.grouproject.models.UserBlock;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;

@Controller
public class AdminController {
    private final Auth auth;
    private final UsersRepository usersRepository;
    private final MatchingProfileRepository matchingProfileRepository;
    private final ChatMessageService chatMessageService;
    private final ExpressionOfInterestRepository expressionOfInterestRepository;
    private final FriendGroupRepository friendGroupRepository;

    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupPreferenceRepository groupPreferenceRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final UserBlockRepository userBlockRepository;

    public AdminController(
            UsersRepository usersRepository,
            Auth auth,
            MatchingProfileRepository matchingProfileRepository,
            ExpressionOfInterestRepository expressionOfInterestRepository,
            FriendGroupRepository friendGroupRepository,
            ChatMessageService chatMessageService,
            GroupMembershipRepository groupMembershipRepository,
            GroupPreferenceRepository groupPreferenceRepository,
            GroupChatMessageRepository groupChatMessageRepository,
            CourseRatingRepository courseRatingRepository,
            UserBlockRepository userBlockRepository) {
        this.usersRepository = usersRepository;
        this.auth = auth;
        this.matchingProfileRepository = matchingProfileRepository;
        this.expressionOfInterestRepository = expressionOfInterestRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.chatMessageService = chatMessageService;

        this.groupMembershipRepository = groupMembershipRepository;
        this.groupPreferenceRepository = groupPreferenceRepository;
        this.groupChatMessageRepository = groupChatMessageRepository;
        this.courseRatingRepository = courseRatingRepository;
        this.userBlockRepository = userBlockRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        // Not an admin/mod -> bounce to the landing page instead of exposing the
        // dashboard.
        if (currentUser == null || currentUser.getRole() == Role.USER) {
            return "redirect:/";
        }

        List<User> users = usersRepository.findAll();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", users);
        return "admin";
    }

    @GetMapping("/admin/groups")
    public String adminGroups(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);
        if (currentUser == null || currentUser.getRole() == Role.USER) {
            return "redirect:/";
        }

        List<FriendGroup> groups = friendGroupRepository.findAll();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("groups", groups);
        return "admin-groups";
    }

    @GetMapping("/account/admincontrols/{id}")
    public String admincontrols_get(@PathVariable("id") long id, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        // Not an admin -> bounce to the landing page instead of exposing the page.
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }
        Optional<User> u = usersRepository.findById(id);
        if (!u.isPresent())
            return "redirect:/admin?error=2"; // not found
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("roles", Role.values());
        model.addAttribute("u", u.get());
        return "admincontrols";
    }

    @PostMapping("/account/admincontrols/{id}")
    @Transactional
    public String admincontrols_post(
            @PathVariable("id") long id,
            @RequestParam Map<String, String> formData,
            HttpSession session,
            Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

        Optional<User> userOptional = usersRepository.findById(id);

        if (userOptional.isEmpty()) {
            return "redirect:/admin?error=2";
        }

        User user = userOptional.get();

        if (formData.containsKey("role")) {
            try {
                Role newRole = Role.valueOf(formData.get("role"));
                Boolean isCAS = Boolean.valueOf(formData.get("isCAS"));

                user.setRole(newRole);
                user.setCAS(isCAS);
                usersRepository.save(user);

                return "redirect:/admin?success=1";
            } catch (Exception exception) {
                return "redirect:/admin?error=1";
            }
        }

        if (!"1".equals(formData.get("delete"))) {
            return "redirect:/account/admincontrols/" + id;
        }

        List<GroupMembership> userMemberships = groupMembershipRepository.findByUserOrderByJoinedAtDesc(user);

        for (GroupMembership membership : userMemberships) { // delete only if not sole admin of a group
            if (membership.getRole() != GroupRole.ADMIN) {
                continue;
            }

            FriendGroup group = membership.getGroup();
            long adminCount = groupMembershipRepository.countByGroupAndRole(
                    group,
                    GroupRole.ADMIN);

            if (adminCount <= 1) {
                return "redirect:/admin?error=3";
            }
        }

        // ownership transfer for safe delete
        List<FriendGroup> createdGroups = friendGroupRepository.findByCreatedBy(user);

        for (FriendGroup group : createdGroups) {
            List<GroupMembership> memberships = groupMembershipRepository.findByGroupOrderByJoinedAtAsc(group);

            User replacementOwner = memberships.stream()
                    .filter(membership -> membership.getRole() == GroupRole.ADMIN)
                    .map(GroupMembership::getUser)
                    .filter(member -> member.getId() != user.getId())
                    .findFirst()
                    .orElse(null);

            if (replacementOwner == null) {
                return "redirect:/admin?error=3";
            }

            group.setCreatedBy(replacementOwner);
            friendGroupRepository.save(group);
        }


        matchingProfileRepository.findByUser(user)
                .ifPresent(matchingProfileRepository::delete);

        groupPreferenceRepository.findByUser(user)
                .ifPresent(groupPreferenceRepository::delete);


        chatMessageService.deleteByUser(user);


        List<ExpressionOfInterest> expressions = expressionOfInterestRepository.findAll();

        for (ExpressionOfInterest expression : expressions) {
            boolean sentByUser = expression.getSender().getId() == user.getId();

            boolean receivedByUser = expression.getReceiver().getId() == user.getId();

            if (sentByUser || receivedByUser) {
                expressionOfInterestRepository.delete(expression);
            }
        }


        List<GroupChatMessage> groupMessages = groupChatMessageRepository.findAll();

        for (GroupChatMessage message : groupMessages) {
            if (message.getSender().getId() == user.getId()) {
                groupChatMessageRepository.delete(message);
            }
        }


        List<CourseRating> courseRatings = courseRatingRepository.findAll();

        for (CourseRating rating : courseRatings) {
            if (rating.getUser().getId() == user.getId()) {
                courseRatingRepository.delete(rating);
            }
        }

        List<UserBlock> userBlocks = userBlockRepository.findAll();

        for (UserBlock block : userBlocks) {
            boolean isBlocker = block.getBlocker().getId() == user.getId();

            boolean isBlocked = block.getBlocked().getId() == user.getId();

            if (isBlocker || isBlocked) {
                userBlockRepository.delete(block);
            }
        }


        for (GroupMembership membership : userMemberships) {
            groupMembershipRepository.delete(membership);
        }

        usersRepository.delete(user);

        return "redirect:/admin?success=1";
    }
}
