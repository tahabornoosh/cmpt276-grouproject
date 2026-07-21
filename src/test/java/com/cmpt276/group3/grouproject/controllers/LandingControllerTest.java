package com.cmpt276.group3.grouproject.controllers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.User;

@WebMvcTest(LandingController.class)
class LandingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Auth auth;

    @Test
    void landing_redirectsToLoginWhenNotAuthenticated()
            throws Exception {

        MockHttpSession session = new MockHttpSession();

        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(get("/").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        verify(auth, never()).getUser(session);
    }

    @Test
    void landing_displaysCurrentUserWhenAuthenticated()
            throws Exception {

        MockHttpSession session = new MockHttpSession();
        User currentUser = new User();

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);

        mockMvc.perform(get("/").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("landing"))
            .andExpect(model().attribute(
                "currentUser",
                currentUser
            ));

        verify(auth).getUser(session);
    }
}
