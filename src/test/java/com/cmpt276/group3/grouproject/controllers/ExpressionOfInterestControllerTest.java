package com.cmpt276.group3.grouproject.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.EOIStream;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterest;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterestRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.models.MatchingProfileRepository;
import com.cmpt276.group3.grouproject.services.ChatMessageService;

@WebMvcTest(ExpressionOfInterestController.class)
class ExpressionOfInterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockitoBean
        private Auth auth;

        @MockitoBean
        private ExpressionOfInterestRepository expressionOfInterestRepository;

        @MockitoBean
        private UsersRepository usersRepository;

        @MockitoBean
        private MatchingProfileRepository matchingProfileRepository;

        @MockitoBean
        private ChatMessageService chatMessageService;

    private MockHttpSession session;
    private User receiver;
    private User sender;
    private ExpressionOfInterest eoi;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();

        receiver = new User(
            "Mike",
            "Student",
            "mike@sfu.ca",
            "pw",
            Role.USER,
            Gender.MALE,
            ""
        );
        receiver.setId(1L);

        sender = new User(
            "Joyce",
            "Student",
            "joyce@sfu.ca",
            "pw",
            Role.USER,
            Gender.FEMALE,
            ""
        );
        sender.setId(2L);

        eoi = new ExpressionOfInterest(
            sender,
            receiver,
            EOIStream.FRIENDSHIP
        );
        eoi.setId(10L);
    }

    @Test
    void eoisPage_redirectsToLoginWhenNotAuthenticated() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(
                get("/eois").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void eoisPage_displaysReceivedEOIsForCurrentUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByReceiverOrderByCreatedAtDesc(receiver)
        ).thenReturn(List.of(eoi));

        mockMvc.perform(
                get("/eois").session(session)
            )
            .andExpect(status().isOk())
            .andExpect(view().name("eois"))
            .andExpect(model().attribute("currentUser", receiver))
            .andExpect(model().attribute("eois", List.of(eoi)));

        verify(expressionOfInterestRepository)
            .findByReceiverOrderByCreatedAtDesc(receiver);
    }

    @Test
    void deleteEOI_deletesEOIBelongingToCurrentUser() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByIdAndReceiver(10L, receiver)
        ).thenReturn(Optional.of(eoi));

        mockMvc.perform(
                post("/eois/10/delete").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/eois?success=deleted"));

        verify(expressionOfInterestRepository).delete(eoi);
    }

    @Test
    void deleteEOI_doesNotDeleteAnotherUsersEOI() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByIdAndReceiver(10L, receiver)
        ).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/eois/10/delete").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/eois?error=not-found"));

        verify(expressionOfInterestRepository, never())
            .delete(any(ExpressionOfInterest.class));
    }
}