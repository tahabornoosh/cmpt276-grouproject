package com.cmpt276.group3.grouproject.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmpt276.group3.grouproject.models.UserBlockRepository;

@RestController
@RequestMapping("/api/zoom")
public class ZoomController {
    
    private final Auth auth;
    private final UserService userService;
    private final UserBlockRepository userBlockRepository;
    private final ZoomService zoomService;
}
