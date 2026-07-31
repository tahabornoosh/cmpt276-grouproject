package com.cmpt276.group3.grouproject.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class CourseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(min=6, max=9)
    private String course;

    @Min(1)
    @Max(5)
    private Integer rating;

    public CourseRating() {}

    public CourseRating(User user, @Size(min = 6, max = 8) String course, @Min(1) @Max(5) Integer rating) {
        this.user = user;
        this.course = course;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }


}