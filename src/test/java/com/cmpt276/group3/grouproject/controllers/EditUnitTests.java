package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.imageio.ImageIO;

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

    private byte[] createValidPng() throws Exception {
        BufferedImage image = new BufferedImage(
            2,
            2,
            BufferedImage.TYPE_INT_RGB
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
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

    @Test
    void edit_missingUser_returns404View() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(admin));

        when(usersRepository.findById(999L))
            .thenReturn(Optional.empty());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        mockMvc.perform(
                get("/account/edit/999").session(session)
            )
            .andExpect(status().isOk())
            .andExpect(view().name("errors/404"));
    }

    @Test
    void edit_passwordChange_hashesNewPassword() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirst_name("Test");
        mockUser.setLast_name("User");
        mockUser.setEmail("test@sfu.ca");
        mockUser.setRole(Role.USER);
        mockUser.setPassword(
            BCrypt.hashpw("oldPassword", BCrypt.gensalt())
        );

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(mockUser));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        mockMvc.perform(
                post("/account/edit/1")
                    .session(session)
                    .param("first_name", "Updated")
                    .param("last_name", "User")
                    .param("gender", "MALE")
                    .param("password", "newPassword")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?success=1")
            );

        assertTrue(
            BCrypt.checkpw("newPassword", mockUser.getPassword())
        );

        verify(usersRepository, times(2)).save(mockUser);
    }

    @Test
    void admin_canEditAnotherUsersAccount() throws Exception {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setFirst_name("Target");
        targetUser.setLast_name("User");
        targetUser.setRole(Role.USER);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(admin));

        when(usersRepository.findById(2L))
            .thenReturn(Optional.of(targetUser));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        mockMvc.perform(
                post("/account/edit/2")
                    .session(session)
                    .param("first_name", "Changed")
                    .param("last_name", "Name")
                    .param("gender", "FEMALE")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/2?success=1")
            );

        assertEquals("Changed", targetUser.getFirst_name());
        assertEquals("Name", targetUser.getLast_name());

        verify(usersRepository).save(targetUser);
    }


    @Test
    void edit_savesTrimmedBio() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        user.setFirst_name("Test");
        user.setLast_name("User");

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        mockMvc.perform(
                multipart("/account/edit/1")
                    .session(session)
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
                    .param("bio", "  Computer science student  ")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?success=1")
            );

        assertEquals(
            "Computer science student",
            user.getBio()
        );

        verify(usersRepository).save(user);
    }

    @Test
    void edit_rejectsBioLongerThan500Characters()
            throws Exception {

        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        mockMvc.perform(
                multipart("/account/edit/1")
                    .session(session)
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
                    .param("bio", "a".repeat(501))
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?error=1")
            );

        assertNull(user.getBio());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void edit_uploadsAndServesValidPngAvatar()
            throws Exception {

        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        byte[] pngBytes = createValidPng();

        MockMultipartFile avatar = new MockMultipartFile(
            "avatarFile",
            "avatar.png",
            MediaType.IMAGE_PNG_VALUE,
            pngBytes
        );

        mockMvc.perform(
                multipart("/account/edit/1")
                    .file(avatar)
                    .session(session)
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
                    .param("bio", "Hello")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?success=1")
            );

        assertArrayEquals(pngBytes, user.getAvatarData());
        assertEquals(
            MediaType.IMAGE_PNG_VALUE,
            user.getAvatarContentType()
        );
        assertEquals("/users/1/avatar", user.getAvatar());

        mockMvc.perform(get("/users/1/avatar"))
            .andExpect(status().isOk())
            .andExpect(
                content().contentType(MediaType.IMAGE_PNG)
            )
            .andExpect(content().bytes(pngBytes));
    }



    @Test
    void edit_rejectsInvalidAvatarContent() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        MockMultipartFile invalidAvatar = new MockMultipartFile(
            "avatarFile",
            "fake.png",
            MediaType.IMAGE_PNG_VALUE,
            "not an image".getBytes()
        );

        mockMvc.perform(
                multipart("/account/edit/1")
                    .file(invalidAvatar)
                    .session(session)
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?avatarError=1")
            );

        assertNull(user.getAvatarData());
        assertNull(user.getAvatarContentType());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void edit_rejectsAvatarLargerThanTwoMegabytes()
            throws Exception {

        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        byte[] oversizedBytes =
            new byte[(2 * 1024 * 1024) + 1];

        MockMultipartFile oversizedAvatar = new MockMultipartFile(
            "avatarFile",
            "large.png",
            MediaType.IMAGE_PNG_VALUE,
            oversizedBytes
        );

        mockMvc.perform(
                multipart("/account/edit/1")
                    .file(oversizedAvatar)
                    .session(session)
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?avatarError=1")
            );

        assertNull(user.getAvatarData());
        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void edit_removesExistingAvatar() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.USER);
        user.setAvatarData(createValidPng());
        user.setAvatarContentType(MediaType.IMAGE_PNG_VALUE);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUserId", 1L);

        mockMvc.perform(
                multipart("/account/edit/1")
                    .session(session)
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
                    .param("removeAvatar", "true")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/account/edit/1?success=1")
            );

        assertNull(user.getAvatarData());
        assertNull(user.getAvatarContentType());
        assertEquals("/user.png", user.getAvatar());
        verify(usersRepository).save(user);
    }

    @Test
    void avatarEndpoint_returns404WhenAvatarDoesNotExist()
            throws Exception {

        User user = new User();
        user.setId(1L);

        when(usersRepository.findById(1L))
            .thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/1/avatar"))
            .andExpect(status().isNotFound());
    }

    @Test
    void editPost_redirectsToLoginWhenUnauthenticated()
            throws Exception {

        mockMvc.perform(
                multipart("/account/edit/1")
                    .param("first_name", "Test")
                    .param("last_name", "User")
                    .param("gender", "MALE")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        verify(usersRepository, never()).save(any(User.class));
    }


}
