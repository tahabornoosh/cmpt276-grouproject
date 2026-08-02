package com.cmpt276.group3.grouproject.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupPreferenceRepository extends JpaRepository<GroupPreference, Long> {

    Optional<GroupPreference> findByUser(User user);

    Optional<GroupPreference> findByUser_Id(long user_Id);

    boolean existsByUser(User user);

    // The auto-matching pool.
    List<GroupPreference> findByLookingForGroupTrue();
}
