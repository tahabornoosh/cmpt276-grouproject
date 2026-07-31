package com.cmpt276.group3.grouproject.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.services.CourseRatingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class CourseRatingController {
    private final CourseRatingService CRS;
    private final Auth auth;

    public CourseRatingController(CourseRatingService CRS, Auth auth) {
        this.CRS=CRS;
        this.auth=auth;
    }

    @GetMapping("/course/ratings")
    public String courseRatingGet(Model model, HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            return "redirect:/login";
        }

        List<CourseRating> ratings = CRS.findAll();
        Map<String, ArrayList<CourseRating>> categorized = new HashMap<String, ArrayList<CourseRating>>();
        // List<String> courses = new ArrayList<String>();
        // List<Double> ratingSum = new ArrayList<Double>();
        // List<Integer> ratingCount = new ArrayList<Integer>();

        for (CourseRating rating:ratings) {
            if (categorized.containsKey(rating.getCourse())) categorized.get(rating.getCourse()).add(rating);
            else {
                categorized.put(rating.getCourse(), new ArrayList<CourseRating>());
                categorized.get(rating.getCourse()).add(rating);
            }
        }

        Map<String, Double> numericalRatings = new HashMap<String, Double>();
        for (Map.Entry<String, ArrayList<CourseRating>> e:categorized.entrySet()) {
            double k = 0;
            double n = 0;
            for (CourseRating c:e.getValue()) {
                k+=c.getRating();
                n+=1;
            }
            numericalRatings.put(e.getKey(), k/n);
        }

        model.addAttribute("ratings", numericalRatings);
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
