package com.cmpt276.group3.grouproject.auth;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;

@Service
public class Auth {
    private static final String sessionKey = "loggedUserId";

    private final UserService userService;

    public Auth(UserService userService) {
        this.userService = userService;
    }

    public boolean isLoggedIn(HttpSession session) { 
        return session.getAttribute(sessionKey) != null;
    }

    public User getUser(HttpSession session) {
        Object userIdObj = session.getAttribute(sessionKey);

        if (userIdObj == null) {
            return null;
        }

        Long userId = (Long) userIdObj;
        return userService.findUserById(userId);
    }

    public void logout(HttpSession session) {
        session.removeAttribute(sessionKey);
        session.invalidate();
    }

    public boolean login(HttpSession session, String email, String password) {
        User user = userService.findUserByEmail(email);

        if (user == null) {
            return false;
        }

        boolean passwordMatch = PasswordUtil.checkPassword(password,user.getPassword());

        if (!passwordMatch) {
            return false;
        }

        session.setAttribute(sessionKey, user.getId());

        return true;
    }
}
