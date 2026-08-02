package com.cmpt276.group3.grouproject.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;



@Controller
public class UsersController {
    private static final long MAX_AVATAR_SIZE_BYTES =
        2L * 1024L * 1024L;

    private final UserService US;
    private final UsersRepository UR;
    private final Auth auth;

    public UsersController(UserService userService, UsersRepository usersRepository) {
        this.US = userService;
        this.UR = usersRepository;
        auth = new Auth(US);
    }

    @GetMapping("/login")
    public String login_controller(Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        if (auth.isLoggedIn(session)) return "redirect:/"; // already logged in
        String CAS_url = CASController.CAS_LOGIN_URL+"?service="+CASController.CAS_SERVICE_URL;
        model.addAttribute("CAS_URL", CAS_url);
        return "login";
    }

    @PostMapping("/process_login")
    public String login_post_controller(@RequestParam Map<String,String> formData, Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        if (auth.isLoggedIn(session)) return "redirect:/";
        if (auth.login(session, formData.get("email"), formData.get("password"))) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            model.addAttribute("user", auth.getUser(session));
            return "redirect:/";
        } else return "redirect:/login?error=1";
    }

    @GetMapping("/signup")
    public String signup_controller(Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        if (auth.isLoggedIn(session)) return "redirect:/"; // already logged in
        return "signup";
    }

    @PostMapping("/process_signup")
    public String signup_post_controller(@RequestParam Map<String,String> formData, Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        if (auth.isLoggedIn(session)) return "redirect:/";

        String firstName = formData.get("first_name");
        String lastName = formData.get("last_name");
        String email = formData.get("email");
        String password = formData.get("password");
        String confirmPassword = formData.get("confirm_password");
        String genderValue = formData.get("gender");

        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()
                || genderValue == null || genderValue.isBlank()) {
            return "redirect:/signup?error=1";
        }

        if (!password.equals(confirmPassword)) {
            return "redirect:/signup?passwordMismatch=1";
        }

        try {
            Gender gender = Gender.valueOf(genderValue);

            User newUser = new User(
                firstName,
                lastName,
                email,
                password,
                Role.USER,
                gender,
                ""
            );

            US.registerUser(newUser);
            return "redirect:/login?registered=1";

        } catch (Exception e) {
            return "redirect:/signup?error=1";
        }
    }

    @GetMapping("/logout")
    public String logout_controller(HttpServletRequest request, HttpServletResponse response) {
        auth.logout(request.getSession());
        return "redirect:/login?success=1";
    }

    @GetMapping("/account/edit/{id}")
    public String getMethodName(@PathVariable("id") long id, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (!auth.isLoggedIn(request.getSession())) return "redirect:/login";
        if (!(auth.getUser(request.getSession()).getId()==id || auth.getUser(request.getSession()).isAdmin())) return "redirect:/";
        model.addAttribute("currentUser", auth.getUser(request.getSession()));
        User u = US.findUserById(id);
        if (u==null) return "errors/404";
        model.addAttribute("user", u);
        model.addAttribute("genders", Gender.values());

        return "editaccount";
    }

    @PostMapping("/account/edit/{id}")
    public String editAccount(
            @PathVariable("id") long id,
            @RequestParam Map<String, String> formData,
            @RequestParam(
                name = "avatarFile",
                required = false
            ) MultipartFile avatarFile,
            @RequestParam(
                name = "removeAvatar",
                required = false,
                defaultValue = "false"
            ) boolean removeAvatar,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        if (!auth.isLoggedIn(request.getSession())) {
            return "redirect:/login";
        }

        User loggedInUser = auth.getUser(request.getSession());

        if (!(loggedInUser.getId() == id || loggedInUser.isAdmin())) {
            return "redirect:/";
        }

        User user = US.findUserById(id);

        if (user == null) {
            return "errors/404";
        }

        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("user", user);
        model.addAttribute("genders", Gender.values());

        String firstName = formData.get("first_name");
        String lastName = formData.get("last_name");
        String genderValue = formData.get("gender");
        String bio = formData.getOrDefault("bio", "").trim();

        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || genderValue == null || genderValue.isBlank()
                || bio.length() > 500) {
            return "redirect:/account/edit/" + id + "?error=1";
        }

        Gender gender;

        try {
            gender = Gender.valueOf(genderValue);
        } catch (IllegalArgumentException exception) {
            return "redirect:/account/edit/" + id + "?error=1";
        }

        if (avatarFile != null
                && !avatarFile.isEmpty()
                && !isValidAvatarUpload(avatarFile)) {
            return "redirect:/account/edit/" + id + "?avatarError=1";
        }

        user.setFirst_name(firstName.trim());
        user.setLast_name(lastName.trim());
        user.setGender(gender);
        user.setBio(bio.isBlank() ? null : bio);

        if (removeAvatar) {
            user.setAvatarData(null);
            user.setAvatarContentType(null);
            user.setAvatar(null);
        } else if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                user.setAvatarData(avatarFile.getBytes());
                user.setAvatarContentType(avatarFile.getContentType());
                user.setAvatar(null);
            } catch (IOException exception) {
                return "redirect:/account/edit/" + id + "?avatarError=1";
            }
        }

        String password = formData.get("password");

        if (password != null && !password.isBlank()) {
            US.updatePassword(user, password);
        }

        UR.save(user);

        return "redirect:/account/edit/" + id + "?success=1";
    }


        @GetMapping("/users/{id}/avatar")
    @ResponseBody
    public ResponseEntity<byte[]> userAvatar(
            @PathVariable("id") long id) {

        User user = US.findUserById(id);

        if (user == null || !user.hasUploadedAvatar()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CACHE_CONTROL,
                "no-cache, no-store, must-revalidate"
            )
            .contentType(
                MediaType.parseMediaType(
                    user.getAvatarContentType()
                )
            )
            .body(user.getAvatarData());
    }

    private boolean isValidAvatarUpload(
            MultipartFile avatarFile) {

        if (avatarFile.getSize() <= 0
                || avatarFile.getSize()
                    > MAX_AVATAR_SIZE_BYTES) {
            return false;
        }

        String contentType = avatarFile.getContentType();

        if (!MediaType.IMAGE_PNG_VALUE.equals(contentType)
                && !MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            return false;
        }

        try {
            BufferedImage image = ImageIO.read(
                new ByteArrayInputStream(
                    avatarFile.getBytes()
                )
            );

            return image != null
                && image.getWidth() > 0
                && image.getHeight() > 0
                && image.getWidth() <= 4096
                && image.getHeight() <= 4096;

        } catch (IOException exception) {
            return false;
        }
    }


/*
    @GetMapping("/testadmin")
    public String testadmin(Model model) {
        User newUser = new User(
                "Admin",
                "User",
                "admin@sfu.ca",
                "admin123",
                Role.MOD,
                Gender.MALE,
                ""
        );

        US.registerUser(newUser);
        return "empty";
    } */

}
