package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.EOIStream;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterest;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterestRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ExpressionOfInterestController {

    private final Auth auth;
    private final ExpressionOfInterestRepository expressionOfInterestRepository;
    private final UsersRepository usersRepository;

    public ExpressionOfInterestController(
            Auth auth,
            ExpressionOfInterestRepository expressionOfInterestRepository,
            UsersRepository usersRepository
            ) {
        this.auth = auth;
        this.expressionOfInterestRepository = expressionOfInterestRepository;
        this.usersRepository = usersRepository;
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

        ArrayList<ExpressionOfInterest> eois_unhidden = new ArrayList<ExpressionOfInterest>();
        ArrayList<ExpressionOfInterest> eois_hidden = new ArrayList<ExpressionOfInterest>();
        for (ExpressionOfInterest e:eois) {
            if (e.isHidden()) eois_hidden.add(e);
            else eois_unhidden.add(e);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("eois", eois_unhidden);
        model.addAttribute("eois_hidden", eois_hidden);

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

    @PostMapping("/eois/{id}/hide")
    public String hideEOI(
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
        ExpressionOfInterest _eoi = eoi.get();
        _eoi.setHidden(!_eoi.isHidden());
        expressionOfInterestRepository.save(_eoi);

        return "redirect:/eois?success=hide";
    }

    @PostMapping("/eoi/send/{id}")
    public String send_eoi(@PathVariable("id") long id, HttpSession session, @RequestParam String stream) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        EOIStream chosenStream = null;
        try {
            chosenStream = EOIStream.valueOf(stream);
        } catch (Exception e) {
            return "redirect:/profile/"+String.valueOf(id)+"?error=1";
        }
        Optional<User> u = usersRepository.findById(id);
        if (u.isEmpty()) return "redirect:/?error=1"; // not found
        User profileUser = u.get();

        List<ExpressionOfInterest> eois = expressionOfInterestRepository.findAll();
        for (ExpressionOfInterest e:eois) {
            if (e.getReceiver().getId()==profileUser.getId() && e.getSender().getId()==currentUser.getId())
                return "redirect:/profile/"+String.valueOf(id)+"?error=2"; // a pending EOI exists
        }

        ExpressionOfInterest eoi = new ExpressionOfInterest(currentUser, profileUser, chosenStream);
        expressionOfInterestRepository.save(eoi);
        return "redirect:/profile/"+String.valueOf(id)+"?success=1";
    }
}
