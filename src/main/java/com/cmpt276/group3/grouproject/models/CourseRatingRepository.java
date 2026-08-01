package com.cmpt276.group3.grouproject.models;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {
    public Optional<CourseRating> findByUserAndCourse(User user, Course course);
    public List<CourseRating> findByCourse(Course course);
}
