package com.cmpt276.group3.grouproject.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import org.springframework.stereotype.Service;

import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.models.Course;
import com.cmpt276.group3.grouproject.models.CourseRatingRepository;
import com.cmpt276.group3.grouproject.models.CourseRepository;
import com.cmpt276.group3.grouproject.models.User;

@Service
public class CourseRatingService {
    private final CourseRatingRepository CRR;
    private final CourseRepository CR;

    public CourseRatingService(CourseRatingRepository CRR, CourseRepository CR) {
        this.CRR = CRR;
        this.CR = CR;
    }

    public List<CourseRating> findByCourse(Course course) {
        return CRR.findByCourse(course);
    }

    public boolean registerRating(User user, String course, int rating) {
        course = course.toUpperCase(); // make uppercase
        Course course_obj = findCourse(course);
        if (course_obj == null)
            return false;
        Optional<CourseRating> c = CRR.findByUserAndCourse(user, course_obj);
        if (c.isPresent()) {
            CourseRating cr = c.get();
            cr.setRating(rating);
            CRR.save(cr);
        } else {
            CourseRating cr = new CourseRating(user, course_obj, rating);
            CRR.save(cr);
        }
        return true;
    }

    public List<CourseRating> findAll() {
        return CRR.findAll();
    }

    private boolean validateCourse(String course) {
        if (Pattern.matches("^[A-Z]{2,4}\s[0-9]{3}$", course))
            return true; // regular course
        else
            return Pattern.matches("^[A-Z]{2,4}\s[0-9]{3}W$", course);
    }

    private Course findCourse(String c) {
        if (!validateCourse(c))
            return null;

        int space = c.indexOf(' ');
        String dep = c.substring(0, space).toLowerCase(Locale.ROOT);
        String code = c.substring(space + 1).toLowerCase(Locale.ROOT);

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();

        String[] terms = { "fall", "summer", "spring" };
        HttpClient client = HttpClient.newHttpClient();

        for (int year = currentYear; year >= currentYear - 1; year--) {
            for (String term : terms) {
                LocalDate termStart = getTermStartDate(year, term);

                // Do not query terms that have not roughly started yet.
                if (today.isBefore(termStart)) {
                    continue;
                }

                String url = String.format(
                        "https://www.sfu.ca/bin/wcm/course-outlines?%d/%s/%s/%s/",
                        year, term, dep, code);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                try {
                    HttpResponse<String> response = client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() != 200) {
                        continue;
                    }

                    JSONArray sections = new JSONArray(response.body());

                    if (!sections.isEmpty()) {
                        String title = sections
                                .getJSONObject(0)
                                .getString("title");

                        List<Course> courses = CR.findAll();
                        for (Course co : courses) {
                            if (co.getCourseCode().equals(code.toUpperCase())
                                    && co.getTopiCode().equals(dep.toUpperCase())) {
                                co.setDescription(title); // update name
                                CR.save(co);
                                return co;
                            }
                        }

                        Course co = new Course(
                                dep.toUpperCase(Locale.ROOT),
                                code.toUpperCase(Locale.ROOT),
                                title);

                        return CR.save(co);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                } catch (IOException | JSONException e) {
                    // Continue checking older terms.
                }
            }
        }

        return null;
    }

    private LocalDate getTermStartDate(int year, String term) {
        return switch (term) {
            case "fall" -> LocalDate.of(year, 9, 1);
            case "summer" -> LocalDate.of(year, 5, 1);
            case "spring" -> LocalDate.of(year, 1, 1);
            default -> throw new IllegalArgumentException("Unknown term: " + term);
        };
    }

}
