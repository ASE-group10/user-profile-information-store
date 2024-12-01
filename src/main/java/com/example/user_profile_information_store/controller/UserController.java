package com.example.user_profile_information_store.controller;

import com.example.user_profile_information_store.model.RoutePreference;
import com.example.user_profile_information_store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/preferences")
    public ResponseEntity<?> getUserPreferences(
            @AuthenticationPrincipal Jwt jwt) {

        String auth0UserId = jwt.getSubject();

        RoutePreference preferences = userService.getUserPreferences(auth0UserId);
        if (preferences != null) {
            return ResponseEntity.ok(preferences);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preferences not found.");
    }
}
