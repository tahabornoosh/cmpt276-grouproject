package com.cmpt276.group3.grouproject.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmpt276.group3.grouproject.enums.Role;

public interface UsersRepository extends JpaRepository<User, Long> {
    public List<User> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}
