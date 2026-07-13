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
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.MatchingProfileService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AdminController {
    private final Auth auth;
    private final UsersRepository usersRepository;
    private final MatchingProfileRepository matchingProfileRepository;

    public AdminController(Auth auth, UsersRepository usersRepository, MatchingProfileRepository matchingProfileRepository) {
        this.auth = auth;
        this.usersRepository = usersRepository;
        this.matchingProfileRepository = matchingProfileRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        // Not an admin/mod -> bounce to the landing page instead of exposing the dashboard.
        if (currentUser == null || currentUser.getRole() == Role.USER) {
            return "redirect:/";
        }

        List<User> users = usersRepository.findAll();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", users);
        return "admin";
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
        if (!u.isPresent()) return "redirect:/admin?error=2"; // not found
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("roles", Role.values());
        model.addAttribute("u", u.get());
        return "admincontrols";
    }

    @PostMapping("/account/admincontrols/{id}")
    public String admincontrols_post(@PathVariable("id") long id, @RequestParam Map<String, String> formData, HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        // Not an admin -> bounce to the landing page instead of exposing the page.
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return "redirect:/";
        }
        Optional<User> u = usersRepository.findById(id);
        if (!u.isPresent()) return "redirect:/admin?error=2"; // not found
        
        if (formData.containsKey("role")) {
            Role newrole = null;
            try {
                newrole = Role.valueOf(formData.get("role"));
            } catch(Exception e) {
                return "redirect:/admin?error=1";
            }

            User us = u.get();
            us.setRole(newrole);
            usersRepository.save(us);
            return "redirect:/admin?success=1";
        } else if (formData.containsKey("delete") && formData.get("delete").equals("1")) {
            Optional<MatchingProfile> profile = matchingProfileRepository.findByUser(u.get());
            if (profile.isPresent()) matchingProfileRepository.delete(profile.get());
            usersRepository.deleteById(id);
            return "redirect:/admin?success=1";
        }

        return "redirect:/account/admincontrols/"+String.valueOf(id);
    }
}
