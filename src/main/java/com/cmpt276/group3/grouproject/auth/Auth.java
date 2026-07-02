package com.cmpt276.group3.grouproject.auth;

import org.springframework.ui.Model;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.User;

import jakarta.servlet.http.HttpSession;

@Service
public class Auth {
    private HttpSession session;

    public Auth(HttpSession session) {
        this.session = session;
    }

    public boolean IsLoggedIn() { 
        return false; // to be implemented
    }

    public User getUser() {
        return null;
    }

    public boolean logout(HttpSession session) {
        return false;
        // to be implemented
    }

    public boolean login(HttpSession session, String email, String password) {
        return false;
        // to be implemented
    }
}
