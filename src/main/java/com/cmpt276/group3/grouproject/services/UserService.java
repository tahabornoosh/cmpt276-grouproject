package com.cmpt276.group3.grouproject.services;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UsersRepository;
import com.cmpt276.group3.grouproject.util.PasswordUtil;

@Service
public class UserService {
    private final UsersRepository userRepository;

    public UserService(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User newUser) {
        
        if (emailExists(newUser.getEmail())) {
            throw new IllegalArgumentException(
                "User with this email already exists"
            );
        }

        
        //Should be validated by controller, placeholder
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                "Password cannot be empty"
            );
        }

        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                "Email cannot be empty"
            );
        }
        //
        
        hashPassword(newUser);
        return userRepository.save(newUser);
    }

    public User updatePassword(User targetUser, String plainPassword) {
        hashPassword(targetUser);
        return userRepository.save(targetUser);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }
    // Helpers

    private void hashPassword(User targetUser) {
        String hashedPassword = PasswordUtil.hashPassword(targetUser.getPassword());
        targetUser.setPassword(hashedPassword);
    } 
}
