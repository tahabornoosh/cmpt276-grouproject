package com.cmpt276.group3.grouproject.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class LandingController {
    private final Auth auth;

    public LandingController(Auth auth) {
        this.auth = auth;
    }

    @GetMapping("/")
    public String landing(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);
        model.addAttribute("currentUser", currentUser);
        return "landing";
    }
}
