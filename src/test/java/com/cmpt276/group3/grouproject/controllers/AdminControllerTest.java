package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Auth auth;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private UserService userService;

    private MockHttpSession session;
    private User adminUser;
    private User modUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        adminUser = new User("Admin", "User", "admin@sfu.ca", "pw", Role.ADMIN, Gender.MALE, "");
        adminUser.setId(1L);

        modUser = new User("Mod", "User", "mod@sfu.ca", "pw", Role.MOD, Gender.MALE, "");
        modUser.setId(2L);

        regularUser = new User("Regular", "User", "user@sfu.ca", "pw", Role.USER, Gender.MALE, "");
        regularUser.setId(3L);
    }

    // --- Task 1: Admin panel must load for admin/mod but redirect for regular user ---

    @Test
    void adminPanel_loadsForAdminUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findAll()).thenReturn(List.of(regularUser));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    void adminPanel_loadsForModUser() throws Exception {
        // MOD role is not Role.USER, so the controller lets them through to the admin view
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(modUser);
        when(usersRepository.findAll()).thenReturn(List.of(regularUser));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    void adminPanel_redirectsForRegularUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(regularUser);

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // --- Task 4: Admin panel must redirect to login for unauthenticated user ---

    @Test
    void adminPanel_redirectsToLoginWhenNotAuthenticated() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
