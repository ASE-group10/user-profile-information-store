package nl.ase_wayfinding.user_profile_information_store.controller;

import nl.ase_wayfinding.user_profile_information_store.dto.AccountUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.PreferencesUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API endpoints related to user operations")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(@AuthenticationPrincipal Jwt jwt) {
        try {
            String auth0UserId = jwt.getSubject();
            Preferences preferences = userService.getPreferences(auth0UserId);
            if (preferences == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preferences not found.");
            }
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve preferences: " + e.getMessage());
        }
    }

    @GetMapping("/account")
    public ResponseEntity<?> getAccountInfo(@AuthenticationPrincipal Jwt jwt) {
        try {
            String auth0UserId = jwt.getSubject();
            User user = userService.getUserByAuth0Id(auth0UserId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
            return ResponseEntity.ok(Map.of(
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "phoneNumber", user.getPhoneNumber(),
                    "picture", user.getPicture(),
                    "createdAt", user.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve account info: " + e.getMessage());
        }
    }


    @PostMapping("/preferences/update")
    public ResponseEntity<?> updatePreferences(@AuthenticationPrincipal Jwt jwt,
                                               @RequestBody PreferencesUpdateRequest request) {
        try {
            String auth0UserId = jwt.getSubject();
            userService.updatePreferences(auth0UserId, request);
            return ResponseEntity.ok("Preferences updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update preferences: " + e.getMessage());
        }
    }

    @PostMapping("/account/update")
    public ResponseEntity<?> updateAccount(@AuthenticationPrincipal Jwt jwt,
                                           @RequestBody AccountUpdateRequest request) {
        try {
            String auth0UserId = jwt.getSubject();
            userService.updateAccount(auth0UserId, request);
            return ResponseEntity.ok("Account updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update account: " + e.getMessage());
        }
    }

}
