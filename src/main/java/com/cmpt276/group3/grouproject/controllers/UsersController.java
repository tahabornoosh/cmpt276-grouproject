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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class UsersController {
    private final UsersRepository UR;

    public UsersController(UsersRepository usersRepository) {
        this.UR = usersRepository;
    }

    @GetMapping("/login")
    public String login_controller(Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        Auth auth = new Auth(session);
        if (auth.IsLoggedIn()) return "redirect:/"; // already logged in
        return "login";
    }

    @PostMapping("/process_login")
    public String login_post_controller(@RequestParam Map<String,String> formData, Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        Auth auth = new Auth(request.getSession());
        if (auth.IsLoggedIn()) return "redirect:/";
        if (auth.login(formData.get("email"), formData.get("password"))) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            model.addAttribute("user", auth.getUser());
            return "redirect:/";
        } else return "redirect:/login?error=1";
    }

    @GetMapping("/signup")
    public String signup_controller(Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        Auth auth = new Auth(session);
        if (auth.IsLoggedIn()) return "redirect:/"; // already logged in
        return "signup";
    }

    @PostMapping("/process_signup")
    public String signup_post_controller(@RequestParam Map<String,String> formData, Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        Auth auth = new Auth(request.getSession());
        return "empty"; // to be implemented
    }
    
    @GetMapping("/logout")
    public String logout_controller(HttpServletRequest request, HttpServletResponse response) {
        Auth auth = new Auth(request.getSession());
        if (auth.logout()) return "redirect:/login?success=1";
        return "redirect:/?error=-1";
    }
    

    /*
    @PostMapping("/testuser")
    public String create_test_user(Model model) {
        User u = new User("Test", "User", "test@example.com", "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", Role.USER, Gender.MALE, ""); // password 1234
        UR.save(u);
        return "empty";
    }
    */

    @GetMapping("/test")
    public String template_test(Model model) {
        return "example";
    }
    
    
}
