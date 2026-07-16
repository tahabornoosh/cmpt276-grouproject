package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterest;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterestRepository;
import com.cmpt276.group3.grouproject.models.User;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ExpressionOfInterestController {

    private final Auth auth;
    private final ExpressionOfInterestRepository expressionOfInterestRepository;

    public ExpressionOfInterestController(
            Auth auth,
            ExpressionOfInterestRepository expressionOfInterestRepository) {
        this.auth = auth;
        this.expressionOfInterestRepository = expressionOfInterestRepository;
    }

    @GetMapping("/eois")
    public String viewEOIs(HttpSession session, Model model) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        List<ExpressionOfInterest> eois =
                expressionOfInterestRepository.findByReceiverOrderByCreatedAtDesc(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("eois", eois);

        return "eois";
    }

    @PostMapping("/eois/{id}/delete")
    public String deleteEOI(
            @PathVariable("id") Long id,
            HttpSession session) {

        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<ExpressionOfInterest> eoi =
                expressionOfInterestRepository.findByIdAndReceiver(id, currentUser);

        if (eoi.isEmpty()) {
            return "redirect:/eois?error=not-found";
        }

        expressionOfInterestRepository.delete(eoi.get());

        return "redirect:/eois?success=deleted";
    }
}
