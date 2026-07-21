package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
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
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cmpt276.group3.grouproject.algorithms.MatchingAlgorithm;
import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.enums.EOIStream;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterest;
import com.cmpt276.group3.grouproject.models.ExpressionOfInterestRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.models.MatchingProfile;
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

    @Test
    void eoisPage_separatesVisibleAndHiddenEOIs() throws Exception {
        ExpressionOfInterest hiddenEOI = new ExpressionOfInterest(
            sender,
            receiver,
            EOIStream.STUDY_BUDDY
        );
        hiddenEOI.setId(11L);
        hiddenEOI.setHidden(true);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByReceiverOrderByCreatedAtDesc(receiver)
        ).thenReturn(List.of(eoi, hiddenEOI));

        mockMvc.perform(
                get("/eois").session(session)
            )
            .andExpect(status().isOk())
            .andExpect(view().name("eois"))
            .andExpect(model().attribute("eois", List.of(eoi)))
            .andExpect(model().attribute(
                "eois_hidden",
                List.of(hiddenEOI)
            ));
    }

    @Test
    void hideEOI_togglesHiddenStatusAndSaves() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByIdAndReceiver(10L, receiver)
        ).thenReturn(Optional.of(eoi));

        mockMvc.perform(
                post("/eois/10/hide").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/eois?success=hide"));

        assertTrue(eoi.isHidden());
        verify(expressionOfInterestRepository).save(eoi);
    }

    @Test
    void hideEOI_doesNotModifyAnotherUsersEOI() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByIdAndReceiver(10L, receiver)
        ).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/eois/10/hide").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/eois?error=not-found"));

        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void acceptEOI_deletesEOIAndCreatesAutomatedMessage()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByIdAndReceiver(10L, receiver)
        ).thenReturn(Optional.of(eoi));

        mockMvc.perform(
                post("/eois/10/accept").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/chat?userId=2"));

        verify(expressionOfInterestRepository).delete(eoi);
        verify(chatMessageService).createMessage(
            receiver,
            2L,
            "(Automated Message) Your EOI Was Accepted!"
        );
    }

    @Test
    void acceptEOI_doesNotAcceptAnotherUsersEOI()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(receiver);
        when(
            expressionOfInterestRepository
                .findByIdAndReceiver(10L, receiver)
        ).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/eois/10/accept").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/eois?error=not-found"));

        verify(expressionOfInterestRepository, never())
            .delete(any(ExpressionOfInterest.class));

        verify(chatMessageService, never()).createMessage(
            any(User.class),
            anyLong(),
            anyString()
        );
    }

    @Test
    void eoiAction_redirectsToLoginWhenNotAuthenticated()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(
                post("/eois/10/accept").session(session)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        verify(expressionOfInterestRepository, never())
            .findByIdAndReceiver(anyLong(), any(User.class));
    }


    @Test
    void sendEOI_savesValidFriendshipEOI() throws Exception {
        MatchingProfile senderProfile = new MatchingProfile();
        senderProfile.setUser(sender);

        MatchingProfile receiverProfile = new MatchingProfile();
        receiverProfile.setUser(receiver);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);
        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(receiver));
        when(matchingProfileRepository.findByUser(sender))
            .thenReturn(Optional.of(senderProfile));
        when(matchingProfileRepository.findByUser(receiver))
            .thenReturn(Optional.of(receiverProfile));
        when(expressionOfInterestRepository.findAll())
            .thenReturn(List.of());

        mockMvc.perform(
                post("/eoi/send/1")
                    .session(session)
                    .param("stream", "FRIENDSHIP")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile/1?success=1"));

        verify(expressionOfInterestRepository)
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_rejectsDuplicatePendingEOI() throws Exception {
        MatchingProfile senderProfile = new MatchingProfile();
        senderProfile.setUser(sender);

        MatchingProfile receiverProfile = new MatchingProfile();
        receiverProfile.setUser(receiver);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);
        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(receiver));
        when(matchingProfileRepository.findByUser(sender))
            .thenReturn(Optional.of(senderProfile));
        when(matchingProfileRepository.findByUser(receiver))
            .thenReturn(Optional.of(receiverProfile));
        when(expressionOfInterestRepository.findAll())
            .thenReturn(List.of(eoi));

        mockMvc.perform(
                post("/eoi/send/1")
                    .session(session)
                    .param("stream", "FRIENDSHIP")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile/1?error=2"));

        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_rejectsInvalidStream() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);

        mockMvc.perform(
                post("/eoi/send/1")
                    .session(session)
                    .param("stream", "INVALID_STREAM")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile/1?error=1"));

        verify(usersRepository, never()).findById(anyLong());
        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_redirectsWhenTargetUserDoesNotExist() throws Exception {
        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);
        when(usersRepository.findById(999L))
            .thenReturn(Optional.empty());

        mockMvc.perform(
                post("/eoi/send/999")
                    .session(session)
                    .param("stream", "FRIENDSHIP")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/?error=1"));

        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_redirectsWhenMatchingProfileIsMissing()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);
        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(receiver));
        when(matchingProfileRepository.findByUser(sender))
            .thenReturn(Optional.empty());

        mockMvc.perform(
                post("/eoi/send/1")
                    .session(session)
                    .param("stream", "FRIENDSHIP")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile/1?error=3"));

        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_rejectsIncompatibleRelationshipMatch()
            throws Exception {

        MatchingProfile senderProfile = new MatchingProfile();
        senderProfile.setUser(sender);

        MatchingProfile receiverProfile = new MatchingProfile();
        receiverProfile.setUser(receiver);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);
        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(receiver));
        when(matchingProfileRepository.findByUser(sender))
            .thenReturn(Optional.of(senderProfile));
        when(matchingProfileRepository.findByUser(receiver))
            .thenReturn(Optional.of(receiverProfile));

        try (
            MockedStatic<MatchingAlgorithm> algorithm =
                mockStatic(MatchingAlgorithm.class)
        ) {
            algorithm.when(
                () -> MatchingAlgorithm.relationshipMatch(
                    senderProfile,
                    receiverProfile
                )
            ).thenReturn(-1);

            mockMvc.perform(
                    post("/eoi/send/1")
                        .session(session)
                        .param("stream", "RELATIONSHIP")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/1?error=3"));
        }

        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_rejectsIncompatibleStudyBuddyMatch()
            throws Exception {

        MatchingProfile senderProfile = new MatchingProfile();
        senderProfile.setUser(sender);

        MatchingProfile receiverProfile = new MatchingProfile();
        receiverProfile.setUser(receiver);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(sender);
        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(receiver));
        when(matchingProfileRepository.findByUser(sender))
            .thenReturn(Optional.of(senderProfile));
        when(matchingProfileRepository.findByUser(receiver))
            .thenReturn(Optional.of(receiverProfile));

        try (
            MockedStatic<MatchingAlgorithm> algorithm =
                mockStatic(MatchingAlgorithm.class)
        ) {
            algorithm.when(
                () -> MatchingAlgorithm.studyBuddyMatch(
                    senderProfile,
                    receiverProfile
                )
            ).thenReturn(-1);

            mockMvc.perform(
                    post("/eoi/send/1")
                        .session(session)
                        .param("stream", "STUDY_BUDDY")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/1?error=3"));
        }

        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

    @Test
    void sendEOI_redirectsToLoginWhenNotAuthenticated()
            throws Exception {

        when(auth.isLoggedIn(session)).thenReturn(false);

        mockMvc.perform(
                post("/eoi/send/1")
                    .session(session)
                    .param("stream", "FRIENDSHIP")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        verify(usersRepository, never()).findById(anyLong());
        verify(expressionOfInterestRepository, never())
            .save(any(ExpressionOfInterest.class));
    }

}