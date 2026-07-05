package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class LoginIntegrationTests {
    @Autowired
    private MockMvc moc;

    @Autowired
    private UserService US;

    @Autowired
    private UsersRepository UR;

    @BeforeEach
    void clear() {
        UR.deleteAll(); // clears everything
    }

    @Test
    void sign_up_and_login() throws Exception {
        moc.perform(post("/process_signup")
            .param("first_name", "Mike")
            .param("last_name", "Test")
            .param("email", "mike@sfu.ca")
            .param("password", "1234")
            .param("confirm_password", "1234")
            .param("gender", "MALE")
            .param("terms", "agree")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registered=1"));

        User created = US.findUserByEmail("mike@sfu.ca");
        assertTrue(created.getFirst_name().equals("Mike"));
        assertTrue(created.getGender()==Gender.MALE);
        assertTrue(created.getRole()==Role.USER);

        moc.perform(post("/process_login")
            .param("email", "mike@sfu.ca")
            .param("password", "1234")
        ).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }
}