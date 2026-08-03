package com.cmpt276.group3.grouproject.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.Course;
import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.models.CourseRepository;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.services.CourseRatingService;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class CourseRatingControllerTest {

    @Mock
    private CourseRatingService courseRatingService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private Auth auth;

    @InjectMocks
    private CourseRatingController courseRatingController;

    @Test
    void courseRatingGet_redirectsWhenNotLoggedIn() {
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);

        when(auth.isLoggedIn(session)).thenReturn(false);

        String view =
            courseRatingController.courseRatingGet(model, session);

        assertEquals("redirect:/login", view);

        verifyNoInteractions(
            courseRepository,
            courseRatingService,
            model
        );
    }

    @Test
    void courseRatingGet_addsAverageRatingsAndCurrentUser() {
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        User currentUser = Mockito.mock(User.class);

        Course firstCourse = Mockito.mock(Course.class);
        Course secondCourse = Mockito.mock(Course.class);

        CourseRating firstRating = Mockito.mock(CourseRating.class);
        CourseRating secondRating = Mockito.mock(CourseRating.class);
        CourseRating thirdRating = Mockito.mock(CourseRating.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);

        when(courseRepository.findAll())
            .thenReturn(List.of(firstCourse, secondCourse));

        when(courseRatingService.findByCourse(firstCourse))
            .thenReturn(List.of(firstRating, secondRating));
        when(courseRatingService.findByCourse(secondCourse))
            .thenReturn(List.of(thirdRating));

        when(firstRating.getRating()).thenReturn(4);
        when(secondRating.getRating()).thenReturn(2);
        when(thirdRating.getRating()).thenReturn(5);

        String view =
            courseRatingController.courseRatingGet(model, session);

        assertEquals("ratings", view);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Course, Double>> ratingsCaptor =
            ArgumentCaptor.forClass(Map.class);

        verify(model).addAttribute(
            Mockito.eq("ratings"),
            ratingsCaptor.capture()
        );
        verify(model).addAttribute("currentUser", currentUser);

        Map<Course, Double> ratings = ratingsCaptor.getValue();

        assertEquals(2, ratings.size());
        assertEquals(3.0, ratings.get(firstCourse));
        assertEquals(5.0, ratings.get(secondCourse));
    }

    @Test
    void courseRatingGet_omitsCoursesWithNoRatings() {
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        User currentUser = Mockito.mock(User.class);
        Course course = Mockito.mock(Course.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(courseRatingService.findByCourse(course))
            .thenReturn(List.of());

        String view =
            courseRatingController.courseRatingGet(model, session);

        assertEquals("ratings", view);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Course, Double>> ratingsCaptor =
            ArgumentCaptor.forClass(Map.class);

        verify(model).addAttribute(
            Mockito.eq("ratings"),
            ratingsCaptor.capture()
        );

        assertEquals(0, ratingsCaptor.getValue().size());
    }

    @Test
    void newRating_redirectsWhenNotLoggedIn() {
        HttpSession session = Mockito.mock(HttpSession.class);

        when(auth.isLoggedIn(session)).thenReturn(false);

        String result =
            courseRatingController.newRating(
                "CMPT 276",
                5,
                session
            );

        assertEquals("redirect:/login", result);
        verifyNoInteractions(courseRatingService);
    }

    @Test
    void newRating_redirectsWithSuccessWhenSaved() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(
            courseRatingService.registerRating(
                currentUser,
                "CMPT 276",
                5
            )
        ).thenReturn(true);

        String result =
            courseRatingController.newRating(
                "CMPT 276",
                5,
                session
            );

        assertEquals(
            "redirect:/course/ratings?success=1",
            result
        );

        verify(courseRatingService)
            .registerRating(currentUser, "CMPT 276", 5);
    }

    @Test
    void newRating_redirectsWithErrorWhenCourseIsInvalid() {
        HttpSession session = Mockito.mock(HttpSession.class);
        User currentUser = Mockito.mock(User.class);

        when(auth.isLoggedIn(session)).thenReturn(true);
        when(auth.getUser(session)).thenReturn(currentUser);
        when(
            courseRatingService.registerRating(
                currentUser,
                "INVALID",
                3
            )
        ).thenReturn(false);

        String result =
            courseRatingController.newRating(
                "INVALID",
                3,
                session
            );

        assertEquals(
            "redirect:/course/ratings?error=1",
            result
        );
    }
}
