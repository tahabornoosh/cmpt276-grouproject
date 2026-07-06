package com.cmpt276.group3.grouproject.services;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Equals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.util.PasswordUtil;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UsersRepository userRepository;

    private final String validPassword = "TestPassword123!";
    // Sign up tests

    @Test
    void validInputs(){
        User newUser = createValidUser();

        // Mimic Repository
        when(userRepository.existsByEmail("testEmail@test.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });
        
        // Begin test
        User result = userService.registerUser(newUser);

        // Asserts
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirst_name());
        assertEquals("Doe", result.getLast_name());
        assertEquals("testEmail@test.com", result.getEmail());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals("AvatarTest", result.getAvatar());
        assertEquals(Role.USER, result.getRole());

        // Check for hash fail
        assertNotEquals(validPassword, result.getPassword());

        // Check for hash match
        assertTrue(PasswordUtil.checkPassword(validPassword, result.getPassword()));

        verify(userRepository).existsByEmail("testEmail@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void dupeEmail(){
        User newUser = createValidUser();
        

        // Mimic Repository

        when(userRepository.existsByEmail("testEmail@test.com"))
                .thenReturn(true);

        // Start Test

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(newUser);
        });

        verify(userRepository).existsByEmail("testEmail@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // Edit Tests

    @Test
    void validPasswordEdit() {
        User userToEdit = createValidUser();

        // Mimic Repository

        /* Password edit does not use findById, Possibly Issue?
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userToEdit));
        */
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });

        // Start test
        User result = userService.updatePassword(userToEdit, "EditedPassword123!");

        // Assert
        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirst_name());
        assertEquals("Doe", result.getLast_name());
        assertEquals("testEmail@test.com", result.getEmail());

        // Check for hash fail
        assertNotEquals("EditedPassword123!", result.getPassword());
        assertFalse(PasswordUtil.checkPassword(validPassword, result.getPassword()));

        assertTrue(PasswordUtil.checkPassword("EditedPassword123!", result.getPassword()));

        verify(userRepository).save(userToEdit);
    }

    @Test
    void emptyPassword() {
        User userToEdit = createValidUser();

        // Mimic Repository
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Start and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updatePassword(userToEdit, "");
        });

        verify(userRepository, never()).save(any(User.class));
    }


    private User createValidUser() {
        User newUser = new User();
        newUser.setFirst_name("John");
        newUser.setLast_name("Doe");
        newUser.setEmail("testEmail@test.com");
        newUser.setPassword(validPassword);
        newUser.setGender(Gender.MALE);
        newUser.setAvatar("AvatarTest");
        newUser.setRole(Role.USER);
        return newUser;
    }
}
