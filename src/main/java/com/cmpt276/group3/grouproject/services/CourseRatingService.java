package com.cmpt276.group3.grouproject.services;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.models.CourseRatingRepository;
import com.cmpt276.group3.grouproject.models.User;

@Service
public class CourseRatingService {
    private final CourseRatingRepository CRR;

    public CourseRatingService(CourseRatingRepository CRR) {
        this.CRR=CRR;
    }

    public boolean registerRating(User user, String course, int rating) {
        course = course.toUpperCase(); // make uppercase
        if (!validateCourse(course) || rating<1 || rating>5) return false; // invalid course name or rating
        Optional<CourseRating> c = CRR.findByUserAndCourse(user, course);
        if (c.isPresent()) {
            CourseRating cr = c.get();
            cr.setRating(rating);
            CRR.save(cr);
        } else {
            CourseRating cr = new CourseRating(user, course, rating);
            CRR.save(cr);
        } return true;
    }

    public List<CourseRating> findAll() {
        return CRR.findAll();
    }

    private boolean validateCourse(String course) {
        return Pattern.matches("^[A-Z]{2,4}\s[1-9]{3}$", course);
    }
}
