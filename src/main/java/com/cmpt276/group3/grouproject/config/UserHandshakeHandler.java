package com.cmpt276.group3.grouproject.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.User;

import jakarta.servlet.http.HttpSession;

@Component
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    
    private final Auth auth;

    public UserHandshakeHandler(Auth auth) {
        this.auth = auth;
    }

    @Override
    protected Principal determineUser(
        ServerHttpRequest request,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return null;
        }

        HttpSession session = servletRequest
                .getServletRequest()
                .getSession(false);

        if (session == null || !auth.isLoggedIn(session)) {
            return null;
        }

        User user = auth.getUser(session);

        if (user == null) {
            return null;
        }

        String userId = String.valueOf(user.getId());

        return () -> userId;
    }
}
