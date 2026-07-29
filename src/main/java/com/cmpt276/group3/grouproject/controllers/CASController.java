package com.cmpt276.group3.grouproject.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CASController {

    private final UserService US;
    private final UsersRepository UR;
    private final Auth auth;
    private final HttpClient httpClient;

    public static final String CAS_VERIFY_ENDPOINT =
            "https://cmpt276-minimal-cas-server.onrender.com/validate/";

    // public static final String CAS_SERVICE_URL = "http://localhost:8080/cas/login"; // for local testing
    public static final String CAS_SERVICE_URL = "https://cmpt276-grouproject-3.onrender.com/cas/login";

    public static final String CAS_LOGIN_URL =
            "https://cmpt276-minimal-cas-server.onrender.com/login";

    public CASController(
            UserService userService,
            UsersRepository usersRepository
    ) {
        this.US = userService;
        this.UR = usersRepository;
        this.auth = new Auth(US);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @GetMapping("/cas/login")
    public String loginCas(
            @RequestParam(required = false) String ticket,
            HttpSession session
    ) {
        if (auth.isLoggedIn(session)) {
            return "redirect:/";
        }

        if (ticket == null || ticket.isBlank()) {
            return "redirect:/login?error=cas";
        }

        Optional<String> verifiedEmail = verifyTicket(ticket);

        if (verifiedEmail.isEmpty()) {
            return "redirect:/login?error=cas";
        }

        String email = verifiedEmail.get().trim().toLowerCase();

        Optional<User> existingUser = UR.findByEmailIgnoreCase(email);

        session.setAttribute("casVerifiedEmail", email);

        if (existingUser.isEmpty()) {
            return "redirect:/cas/new";
        }

        User user = existingUser.get();

        if (user.isCAS()) {
            auth.login(session, email);
            return "redirect:/";
        }

        return "redirect:/cas/convert";
    } 

    @GetMapping("/cas/convert")
    public String convertCas(Model model, HttpSession session) {
        if (auth.isLoggedIn(session)) return "redirect:/";
        String email;
        try {
            email = (String) session.getAttribute("casVerifiedEmail"); 
        } catch (Exception e) {
            return "redirect:/";
        }
        if (email==null || email=="") return "redirect:/";
        Optional<User> userO = UR.findByEmailIgnoreCase(email);
        if (userO.isEmpty() || userO.get().isCAS()) {
            session.setAttribute("casVerifiedEmail", "");
            return "redirect:/";
        }
        return "cas_convert";
    }

    @PostMapping("/cas/convert")
    public String convertCasPost(@RequestParam String password, Model model, HttpSession session) {
        if (auth.isLoggedIn(session)) return "redirect:/";
        String email;
        try {
            email = (String) session.getAttribute("casVerifiedEmail"); 
        } catch (Exception e) {
            return "redirect:/";
        }
        if (email==null || email=="") return "redirect:/";
        Optional<User> userO = UR.findByEmailIgnoreCase(email);
        if (userO.isEmpty() || userO.get().isCAS()) {
            session.setAttribute("casVerifiedEmail", "");
            return "redirect:/";
        }
        
        if (!auth.login(session, email, password)) {
            return "redirect:/cas/convert?error=1";
        }

        User u = auth.getUser(session);
        u.setCAS(true);
        UR.save(u);
        session.setAttribute("casVerifiedEmail", "");
        return "redirect:/";
    }

    @GetMapping("/cas/new")
    public String newCas(Model model, HttpSession session) {
        if (auth.isLoggedIn(session)) return "redirect:/";
        String email;
        try {
            email = (String) session.getAttribute("casVerifiedEmail"); 
        } catch (Exception e) {
            return "redirect:/";
        }
        if (email==null || email=="") return "redirect:/";
        Optional<User> userO = UR.findByEmailIgnoreCase(email);
        if (userO.isPresent()) {
            session.setAttribute("casVerifiedEmail", "");
            return "redirect:/";
        }
        return "cas_new";
    }

    @PostMapping("/cas/new")
    public String newCasPost(@RequestParam Map<String, String> formData, Model model, HttpSession session) {
        if (auth.isLoggedIn(session)) return "redirect:/";
        String email;
        try {
            email = (String) session.getAttribute("casVerifiedEmail"); 
        } catch (Exception e) {
            return "redirect:/";
        }
        if (email==null || email=="") return "redirect:/";
        Optional<User> userO = UR.findByEmailIgnoreCase(email);
        if (userO.isPresent()) {
            session.setAttribute("casVerifiedEmail", "");
            return "redirect:/";
        }
        
        if (formData.get("first_name") == null || formData.get("first_name").isBlank()
                || formData.get("last_name") == null || formData.get("last_name").isBlank()
                || formData.get("gender") == null || formData.get("gender").isBlank()) {
            return "redirect:/cas/new?error=1";
        }

        try {
            Gender gender = Gender.valueOf(formData.get("gender"));

            User newUser = new User(
                formData.get("first_name"),
                formData.get("last_name"),
                email,
                "",
                Role.USER,
                gender,
                ""
            );

            US.registerCasUser(newUser);

            if (!auth.login(session, email)) return "redirect:/cas/new?error=2";

        } catch (Exception e) {
            return "redirect:/cas/new?error=3";
        } 
        session.setAttribute("casVerifiedEmail", "");
        return "redirect:/";
    }
    

    private Optional<String> verifyTicket(String ticket) {
        String validationUrl = CAS_VERIFY_ENDPOINT
                + "?service=" + encode(CAS_SERVICE_URL)
                + "&ticket=" + encode(ticket);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(validationUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "text/plain")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() != 200) {
                return Optional.empty();
            }

            String body = response.body();

            if (body == null || body.isBlank()) {
                return Optional.empty();
            }

            String[] lines = body.strip().split("\\R");

            if (lines.length < 2) {
                return Optional.empty();
            }

            if (!"yes".equalsIgnoreCase(lines[0].trim())) {
                return Optional.empty();
            }

            String email = lines[1].trim();

            if (email.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(email);

        } catch (IOException exception) {
            return Optional.empty();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}