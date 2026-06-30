package com.cmpt276.group3.grouproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
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
    public String login_controller(Model model) {
        return "empty"; // to be implemented
    }

    @PostMapping("/process_login")
    public String login_post_controller(@RequestParam String email, @RequestParam String password, Model model) {
        return "empty"; // to be implemented
    }
    
    @GetMapping("/logout")
    public String logout_controller(@RequestParam String param, Model model) {
        return "empty"; // to be implemented
    }
    

    /*
    @PostMapping("/testuser")
    public String create_test_user(Model model) {
        User u = new User("Test", "User", "test@example.com", "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", Role.USER, Gender.MALE, ""); // password 1234
        UR.save(u);
        return "empty";
    }
    */
    
}
