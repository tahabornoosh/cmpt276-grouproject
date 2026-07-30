package com.cmpt276.group3.grouproject.models;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {
    public Optional<CourseRating> findByUserAndCourse(User user, String course);
}
