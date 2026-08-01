package com.cmpt276.group3.grouproject.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.Course;
import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.models.CourseRepository;
import com.cmpt276.group3.grouproject.services.CourseRatingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class CourseRatingController {
    private final CourseRatingService CRS;
    private final CourseRepository CR;
    private final Auth auth;

    public CourseRatingController(CourseRatingService CRS, Auth auth, CourseRepository CR) {
        this.CRS=CRS;
        this.auth=auth;
        this.CR = CR;
    }

    @GetMapping("/course/ratings")
    public String courseRatingGet(Model model, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        Map<Course, Double> ratings = new HashMap<Course, Double>();

        List<Course> courses = CR.findAll();
        for (Course course: courses) {
            List<CourseRating> course_ratings = CRS.findByCourse(course);
            int count = 0;
            int sum = 0;
            for (CourseRating r:course_ratings) {
                sum+=r.getRating();
                count++;
            } if (count!=0) {
                ratings.put(course, (double) sum/count);
            }
        }


        model.addAttribute("ratings", ratings);
        model.addAttribute("currentUser", auth.getUser(session));
        return "ratings";
    }

    @PostMapping("/course/new_rating")
    public String newRating(@RequestParam String course, @RequestParam int rating, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        if (CRS.registerRating(auth.getUser(session), course, rating)) return "redirect:/course/ratings?success=1";
        return "redirect:/course/ratings?error=1";
    }
    
}
