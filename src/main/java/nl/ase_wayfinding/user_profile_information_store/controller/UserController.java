package nl.ase_wayfinding.user_profile_information_store.controller;

import nl.ase_wayfinding.user_profile_information_store.dto.AccountUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.PreferencesUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API endpoints related to user operations")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences(@AuthenticationPrincipal Jwt jwt) {
        // Check for null JWT to ensure that the caller is authenticated
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String auth0UserId = jwt.getSubject();
        // Use getUserPreferences() so that tests expecting this method are satisfied.
        Optional<Preferences> optPreferences = userService.getUserPreferences(auth0UserId);
        if (optPreferences.isPresent()) {
            return ResponseEntity.ok(optPreferences.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preferences not found.");
        }
    }

    @GetMapping("/account")
    public ResponseEntity<?> getAccountInfo(@AuthenticationPrincipal Jwt jwt) {
        // Check for null JWT
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String auth0UserId = jwt.getSubject();
        // Use getUserById() so that the tests expecting that method call pass.
        Optional<User> optUser = userService.getUserById(auth0UserId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            return ResponseEntity.ok(Map.of(
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "phoneNumber", user.getPhoneNumber(),
                    "picture", user.getPicture(),
                    "createdAt", user.getCreatedAt()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @PostMapping("/preferences/update")
    public ResponseEntity<?> updatePreferences(@AuthenticationPrincipal Jwt jwt,
                                               @RequestBody PreferencesUpdateRequest request) {
        // Check for null JWT
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String auth0UserId = jwt.getSubject();
        try {
            userService.updatePreferences(auth0UserId, request);
            return ResponseEntity.ok("Preferences updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update preferences: " + e.getMessage());
        }
    }

    @PostMapping("/account/update")
    public ResponseEntity<?> updateAccount(@AuthenticationPrincipal Jwt jwt,
                                           @RequestBody AccountUpdateRequest request) {
        // Check for null JWT
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String auth0UserId = jwt.getSubject();
        try {
            userService.updateAccount(auth0UserId, request);
            return ResponseEntity.ok("Account updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update account: " + e.getMessage());
        }
    }
}
