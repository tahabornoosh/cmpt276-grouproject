package com.cmpt276.group3.grouproject.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ChatController {
    private final Auth auth;
    private final UsersRepository usersRepository;
    private final MatchingProfileRepository matchingProfileRepository;

    public ChatController(Auth auth, UsersRepository usersRepository, MatchingProfileRepository matchingProfileRepository) {
        this.auth = auth;
        this.usersRepository = usersRepository;
        this.matchingProfileRepository = matchingProfileRepository;
    }


    @GetMapping("/chat")
    public String load_chat(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }
        User currentUser = auth.getUser(session);
        model.addAttribute("currentUser", currentUser);
        return "chat"; // rest to be implemented
    }
    
}
