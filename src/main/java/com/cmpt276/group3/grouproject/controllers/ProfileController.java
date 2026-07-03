package com.cmpt276.group3.grouproject.controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {
    private final Auth auth;
    private final UsersRepository usersRepository;

    public ProfileController(Auth auth, UsersRepository usersRepository) {
        this.auth = auth;
        this.usersRepository = usersRepository;
    }

    // Public within the app: any logged-in user can view any other user's public profile.
    // Once Taha's Questionnaire model lands, this is where interests/preferences get added
    // to the model before rendering.
    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        Optional<User> profileUser = usersRepository.findById(id);
        if (profileUser.isEmpty()) {
            return "redirect:/";
        }

        User viewer = auth.getUser(session);
        boolean isOwnProfile = viewer != null && viewer.getId() == profileUser.get().getId();

        model.addAttribute("profileUser", profileUser.get());
        model.addAttribute("currentUser", viewer);
        model.addAttribute("isOwnProfile", isOwnProfile);
        return "profile";
    }
}
