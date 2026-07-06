package com.cmpt276.group3.grouproject.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {
    private final Auth auth;
    private final UsersRepository usersRepository;

    public AdminController(Auth auth, UsersRepository usersRepository) {
        this.auth = auth;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        // Not an admin -> bounce to the landing page instead of exposing the dashboard.
        if (currentUser == null || currentUser.getRole() == Role.USER) {
            return "redirect:/";
        }

        // Admins manage regular accounts; admin accounts themselves aren't listed here.
        List<User> users = usersRepository.findAll();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", users);
        return "admin";
    }
}
