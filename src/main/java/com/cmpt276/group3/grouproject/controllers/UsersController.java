package com.cmpt276.group3.grouproject.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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



@Controller
public class UsersController {
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
    public String getMethodName(@PathVariable("id") long id, @RequestParam Map<String, String> formData, HttpServletRequest request, HttpServletResponse response, Model model) {
        if (!auth.isLoggedIn(request.getSession())) return "redirect:/login";
        if (!(auth.getUser(request.getSession()).getId()==id || auth.getUser(request.getSession()).isAdmin())) return "redirect:/";
        model.addAttribute("currentUser", auth.getUser(request.getSession()));
        User u = US.findUserById(id);
        if (u==null) return "errors/404";
        model.addAttribute("user", u);
        model.addAttribute("genders", Gender.values());

        if (!formData.containsKey("first_name") || formData.get("first_name")=="") return "redirect:/account/edit/"+String.valueOf(id)+"?error=1";
        if (!formData.containsKey("last_name") || formData.get("last_name")=="") return "redirect:/account/edit/"+String.valueOf(id)+"?error=1";
        if (!formData.containsKey("gender") || formData.get("gender")=="") return "redirect:/account/edit/"+String.valueOf(id)+"?error=1";

        u.setFirst_name(formData.get("first_name"));
        u.setLast_name(formData.get("last_name"));
        u.setGender(Gender.valueOf(formData.get("gender")));
        
        if (formData.containsKey("password") && formData.get("password")!="")  {
            u.setPassword(formData.get("password"));
            US.updatePassword(u, formData.get("password"));
        }

        UR.save(u);
        return "redirect:/account/edit/"+String.valueOf(id)+"?success=1";
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
