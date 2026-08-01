package com.cmpt276.group3.grouproject.controllers;

import com.cmpt276.group3.grouproject.services.UserService;
import com.cmpt276.group3.grouproject.services.ZoomService;
import com.cmpt276.group3.grouproject.util.zoom.CreateZoomMeetingRequest;
import com.cmpt276.group3.grouproject.util.zoom.ZoomMeetingResponse;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cmpt276.group3.grouproject.auth.Auth;
import com.cmpt276.group3.grouproject.models.User;
import com.cmpt276.group3.grouproject.models.UserBlockRepository;

@RestController
@RequestMapping("/api/zoom")
public class ZoomController {
    
    private final Auth auth;
    private final UserService userService;
    private final UserBlockRepository userBlockRepository;
    private final ZoomService zoomService;

    public ZoomController(
        Auth auth,
        UserService userService, UserService userService_1,
        UserBlockRepository userBlockRepository,
        ZoomService zoomService
    ) {
        this.auth = auth;
        this.userService = userService;
        this.userBlockRepository = userBlockRepository;
        this.zoomService = zoomService;
    }

    @PostMapping("/meetings")
    public ZoomMeetingResponse createMeeting(
        @RequestBody CreateZoomMeetingRequest request,
        HttpSession session
    ) {
        User currentUser = requireLoggedInUser(session);

        if (request == null || request.getRecipientId() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A recipient ID is required"
            );
        }

        User recipient = requireOtherUser(currentUser, request.getRecipientId());

        requireCommunicationAllowed(currentUser, recipient);

        return zoomService.createMeeting();
    }

    private User requireLoggedInUser(HttpSession session) {
        if (!auth.isLoggedIn(session)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "You must be logged in"
            );
        }

        User currentUser = auth.getUser(session);

        if (currentUser == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "The logged-in user could not be found"
            );
        }

        return currentUser;
    }

    private User requireOtherUser(
        User currentUser,
        long otherUserId
    ) {
        if (currentUser.getId() == otherUserId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot start a call with yourself"
            );
        }

        User otherUser = userService.findUserById(otherUserId);

        if (otherUser == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The other user was not found"
            );
        }
        return otherUser;
    }

    private void requireCommunicationAllowed(
        User currentUser,
        User otherUser
    ) {
        boolean blockedByCurrentUser = 
            userBlockRepository.existsByBlockerIdAndBlockedId(currentUser.getId(), otherUser.getId());

        boolean blockedByOtherUser =
            userBlockRepository.existsByBlockerIdAndBlockedId(otherUser.getId(), currentUser.getId());

        if (blockedByCurrentUser || blockedByOtherUser) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User is blocked"
            );
        }
    }
}
