package com.cmpt276.group3.grouproject.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, Integer> {
    public List<User> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
