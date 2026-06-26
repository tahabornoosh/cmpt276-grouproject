package com.cmpt276.group3.grouproject.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, Integer> {
    public User findByEmailAndPassword(String email, String password);
}
