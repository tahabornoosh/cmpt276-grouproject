package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

public class EditUnitTests {
    private MockMvc mockMvc;

    @Mock
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UserService userService = new UserService(usersRepository);
        UsersController usersController = new UsersController(userService, usersRepository);

        ViewResolver viewResolver = new ViewResolver() {
            @Override
            public View resolveViewName(String viewName, Locale locale) {
                if (viewName.startsWith("redirect:")) {
                    return new org.springframework.web.servlet.view.RedirectView(
                            viewName.substring("redirect:".length()),
                            true
                    );
                }

                return (model, request, response) -> {
                    // Fake normal view. Prevents circular view path for "login".
                };
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(usersController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void edit_authentication_check() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("test@sfu.ca");
        mockUser.setId(1);

        String hashedPassword = BCrypt.hashpw("12345", BCrypt.gensalt());
        mockUser.setPassword(hashedPassword);

        when(usersRepository.findByEmail("test@sfu.ca"))
                .thenReturn(Optional.of(mockUser));
        
        when(usersRepository.findById(1l))
                .thenReturn(Optional.of(mockUser));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1l);

        mockMvc.perform(get("/account/edit/1")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/account/edit/1").session(session)).andExpect(status().is2xxSuccessful());
        mockMvc.perform(get("/account/edit/2").session(session)).andExpect(status().is3xxRedirection()); // should not allow editing others' accounts
    }

    @Test
    void edit_valid_invalid() throws Exception {
        User mockUser = new User();
        mockUser.setFirst_name("Test");
        mockUser.setLast_name("Test");
        mockUser.setEmail("test@sfu.ca");
        mockUser.setId(1);

        String hashedPassword = BCrypt.hashpw("12345", BCrypt.gensalt());
        mockUser.setPassword(hashedPassword);

        when(usersRepository.findByEmail("test@sfu.ca"))
                .thenReturn(Optional.of(mockUser));
        
        when(usersRepository.findById(1l))
                .thenReturn(Optional.of(mockUser));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1l);

        mockMvc.perform(post("/account/edit/1").session(session)
            .param("first_name", "Test2")
            .param("last_name", "Test2")
            .param("gender", "MALE")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/account/edit/1?success=1"));

        mockMvc.perform(post("/account/edit/1").session(session)
            .param("first_name", "") // empty
            .param("last_name", "Test2")
            .param("gender", "MALE")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/account/edit/1?error=1"));
    }
}
