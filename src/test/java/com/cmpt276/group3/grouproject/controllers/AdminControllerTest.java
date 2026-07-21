package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Auth auth;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MatchingProfileRepository matchingProfileRepository;

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

    @Test
    void adminControls_loadsForAdminUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findById(3L))
                .thenReturn(Optional.of(regularUser));

        mockMvc.perform(
                        get("/account/admincontrols/3")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admincontrols"))
                .andExpect(model().attribute("currentUser", adminUser))
                .andExpect(model().attribute("u", regularUser));
    }

    @Test
    void adminControls_redirectsModeratorToHome() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(modUser);

        mockMvc.perform(
                        get("/account/admincontrols/3")
                                .session(session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(usersRepository, never()).findById(3L);
    }

    @Test
    void roleChange_updatesAndSavesUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findById(3L))
                .thenReturn(Optional.of(regularUser));

        mockMvc.perform(
                        post("/account/admincontrols/3")
                                .session(session)
                                .param("role", "MOD")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?success=1"));

        assertEquals(Role.MOD, regularUser.getRole());
        verify(usersRepository).save(regularUser);
    }

    @Test
    void roleChange_rejectsInvalidRole() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findById(3L))
                .thenReturn(Optional.of(regularUser));

        mockMvc.perform(
                        post("/account/admincontrols/3")
                                .session(session)
                                .param("role", "INVALID_ROLE")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?error=1"));

        assertEquals(Role.USER, regularUser.getRole());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_deletesProfileAndUser() throws Exception {
        MatchingProfile profile = new MatchingProfile();
        profile.setUser(regularUser);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findById(3L))
                .thenReturn(Optional.of(regularUser));
        when(matchingProfileRepository.findByUser(regularUser))
                .thenReturn(Optional.of(profile));

        mockMvc.perform(
                        post("/account/admincontrols/3")
                                .session(session)
                                .param("delete", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?success=1"));

        verify(matchingProfileRepository).delete(profile);
        verify(usersRepository).deleteById(3L);
    }

    @Test
    void deleteUser_worksWhenUserHasNoProfile() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findById(3L))
                .thenReturn(Optional.of(regularUser));
        when(matchingProfileRepository.findByUser(regularUser))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/account/admincontrols/3")
                                .session(session)
                                .param("delete", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?success=1"));

        verify(matchingProfileRepository, never())
                .delete(any(MatchingProfile.class));
        verify(usersRepository).deleteById(3L);
    }

    @Test
    void adminControls_redirectsWhenTargetUserDoesNotExist() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(adminUser);
        when(usersRepository.findById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/account/admincontrols/999")
                                .session(session)
                                .param("role", "MOD")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?error=2"));

        verify(usersRepository, never()).save(any(User.class));
        verify(usersRepository, never()).deleteById(999L);
    }

    @Test
    void adminControlsPost_redirectsToLoginWhenNotAuthenticated()
            throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(
                        post("/account/admincontrols/3")
                                .session(session)
                                .param("delete", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(usersRepository, never()).findById(3L);
    }

}
