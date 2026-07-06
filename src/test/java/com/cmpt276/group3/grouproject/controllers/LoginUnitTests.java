package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

public class LoginUnitTests {

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
    void load_login_page() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_success() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("test@sfu.ca");

        String hashedPassword = BCrypt.hashpw("12345", BCrypt.gensalt());
        mockUser.setPassword(hashedPassword);

        when(usersRepository.findByEmail("test@sfu.ca"))
                .thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/process_login")
                .param("email", "test@sfu.ca")
                .param("password", "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void login_fail() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("test@sfu.ca");

        String hashedPassword = BCrypt.hashpw("12345", BCrypt.gensalt());
        mockUser.setPassword(hashedPassword);

        when(usersRepository.findByEmail("test@sfu.ca"))
                .thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/process_login")
                .param("email", "test@sfu.ca")
                .param("password", "123456")) // wrong password
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=1"));
        
        mockMvc.perform(post("/process_login")
                .param("email", "test1@sfu.com") // wrong email
                .param("password", "12345")) 
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=1"));
    }

    @Test
    void signup_success() throws Exception {
        mockMvc.perform(post("/process_signup")
            .param("first_name", "Mike")
            .param("last_name", "Test")
            .param("email", "mike@sfu.ca")
            .param("password", "1234")
            .param("confirm_password", "1234")
            .param("gender", "MALE")
            .param("terms", "agree")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered=1"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(savedUser.getFirst_name(), "Mike");
        assertEquals(savedUser.getEmail(), "mike@sfu.ca");
        assertEquals(savedUser.getRole(), Role.USER);        
    }

    @Test
    void signup_fail() throws Exception {
        mockMvc.perform(post("/process_signup")
            //.param("first_name", "Mike") missing first name
            .param("last_name", "Test")
            .param("email", "mike@sfu.ca")
            .param("password", "1234")
            .param("confirm_password", "1234")
            .param("gender", "MALE")
            .param("terms", "agree")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/signup?error=1"));
        
        verify(usersRepository, never()).save(any(User.class));

        mockMvc.perform(post("/process_signup")
            .param("first_name", "Mike") 
            .param("last_name", "Test")
            .param("email", "mike@sfu.ca")
            .param("password", "1234")
            .param("confirm_password", "12345")
            .param("gender", "MALE")
            .param("terms", "agree")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/signup?passwordMismatch=1"));
    }
}