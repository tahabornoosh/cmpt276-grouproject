package com.cmpt276.group3.grouproject.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cmpt276.group3.grouproject.models.Course;
import com.cmpt276.group3.grouproject.models.CourseRating;
import com.cmpt276.group3.grouproject.models.CourseRatingRepository;
import com.cmpt276.group3.grouproject.models.CourseRepository;
import com.cmpt276.group3.grouproject.models.User;

@ExtendWith(MockitoExtension.class)
class CourseRatingServiceTest {

    @Mock
    private CourseRatingRepository courseRatingRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseRatingService courseRatingService;

    @Test
    void findByCourse_returnsRepositoryResults() {
        Course course = Mockito.mock(Course.class);
        CourseRating rating = Mockito.mock(CourseRating.class);
        List<CourseRating> expected = List.of(rating);

        when(courseRatingRepository.findByCourse(course))
            .thenReturn(expected);

        List<CourseRating> actual =
            courseRatingService.findByCourse(course);

        assertSame(expected, actual);
        verify(courseRatingRepository).findByCourse(course);
    }

    @Test
    void findAll_returnsRepositoryResults() {
        CourseRating rating = Mockito.mock(CourseRating.class);
        List<CourseRating> expected = List.of(rating);

        when(courseRatingRepository.findAll())
            .thenReturn(expected);

        List<CourseRating> actual = courseRatingService.findAll();

        assertSame(expected, actual);
        verify(courseRatingRepository).findAll();
    }

    @Test
    void registerRating_rejectsMalformedCourse() {
        User user = Mockito.mock(User.class);

        boolean result =
            courseRatingService.registerRating(
                user,
                "not a course",
                5
            );

        assertFalse(result);

        verifyNoInteractions(
            courseRepository,
            courseRatingRepository
        );
    }

    @Test
    void registerRating_createsNewCourseAndRating()
        throws Exception {

        User user = Mockito.mock(User.class);
        HttpClient client = Mockito.mock(HttpClient.class);

        @SuppressWarnings("unchecked")
        HttpResponse<String> response =
            Mockito.mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(
            "[{\"title\":\"Software Engineering\"}]"
        );

        when(
            client.send(
                any(HttpRequest.class),
                Mockito.<HttpResponse.BodyHandler<String>>any()
            )
        ).thenReturn(response);

        when(courseRepository.findAll()).thenReturn(List.of());
        when(courseRepository.save(any(Course.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(
            courseRatingRepository.findByUserAndCourse(
                any(User.class),
                any(Course.class)
            )
        ).thenReturn(Optional.empty());

        when(courseRatingRepository.save(any(CourseRating.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        try (
            MockedStatic<HttpClient> factory =
                Mockito.mockStatic(HttpClient.class)
        ) {
            factory.when(HttpClient::newHttpClient)
                .thenReturn(client);

            boolean result =
                courseRatingService.registerRating(
                    user,
                    "cmpt 276",
                    5
                );

            assertTrue(result);
        }

        ArgumentCaptor<Course> courseCaptor =
            ArgumentCaptor.forClass(Course.class);

        verify(courseRepository).save(courseCaptor.capture());

        Course savedCourse = courseCaptor.getValue();

        assertEquals("CMPT", savedCourse.getTopiCode());
        assertEquals("276", savedCourse.getCourseCode());
        assertEquals(
            "Software Engineering",
            savedCourse.getDescription()
        );

        ArgumentCaptor<CourseRating> ratingCaptor =
            ArgumentCaptor.forClass(CourseRating.class);

        verify(courseRatingRepository)
            .save(ratingCaptor.capture());

        CourseRating savedRating = ratingCaptor.getValue();

        assertSame(user, savedRating.getUser());
        assertSame(savedCourse, savedRating.getCourse());
        assertEquals(5, savedRating.getRating());
    }

    @Test
    void registerRating_updatesExistingRating()
        throws Exception {

        User user = Mockito.mock(User.class);
        HttpClient client = Mockito.mock(HttpClient.class);

        Course existingCourse =
            new Course("CMPT", "276", "Old title");

        CourseRating existingRating =
            new CourseRating(user, existingCourse, 2);

        @SuppressWarnings("unchecked")
        HttpResponse<String> response =
            Mockito.mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(
            "[{\"title\":\"Software Engineering\"}]"
        );

        when(
            client.send(
                any(HttpRequest.class),
                Mockito.<HttpResponse.BodyHandler<String>>any()
            )
        ).thenReturn(response);

        when(courseRepository.findAll())
            .thenReturn(List.of(existingCourse));

        when(
            courseRatingRepository.findByUserAndCourse(
                user,
                existingCourse
            )
        ).thenReturn(Optional.of(existingRating));

        try (
            MockedStatic<HttpClient> factory =
                Mockito.mockStatic(HttpClient.class)
        ) {
            factory.when(HttpClient::newHttpClient)
                .thenReturn(client);

            boolean result =
                courseRatingService.registerRating(
                    user,
                    "CMPT 276",
                    4
                );

            assertTrue(result);
        }

        assertEquals(
            "Software Engineering",
            existingCourse.getDescription()
        );
        assertEquals(4, existingRating.getRating());

        verify(courseRepository).save(existingCourse);
        verify(courseRatingRepository).save(existingRating);
    }
}
