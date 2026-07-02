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

        String hashedPassword = PasswordUtil.hashPassword(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        return userRepository.save(newUser);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
