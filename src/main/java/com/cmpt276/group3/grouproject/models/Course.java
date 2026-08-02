package com.cmpt276.group3.grouproject.models;

import jakarta.persistence.*;

@Entity
@Table(name="course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topiCode;

    private String courseCode;

    private String description;

    public Course() {}

    public Course(String topiCode, String courseCode, String description) {
        this.topiCode = topiCode;
        this.courseCode = courseCode;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopiCode() {
        return topiCode;
    }

    public void setTopiCode(String topiCode) {
        this.topiCode = topiCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return this.topiCode+" "+this.courseCode+" "+this.description;
    }

}
