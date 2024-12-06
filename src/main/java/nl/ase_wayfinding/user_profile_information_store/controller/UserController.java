package nl.ase_wayfinding.user_profile_information_store.controller;

import nl.ase_wayfinding.user_profile_information_store.model.RoutePreference;
import nl.ase_wayfinding.user_profile_information_store.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API endpoints related to user operations")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
        summary = "Get User Preferences", 
        description = "Retrieve user preferences based on the Auth0 user ID from the JWT token.",
        tags = { "User API" }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved preferences",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RoutePreference.class),
                examples = @ExampleObject(
                    name = "200 OK Example",
                    value = "{\n" +
                            "  \"preferenceId\": 2,\n" +
                            "  \"user\": {\n" +
                            "    \"id\": 2,\n" +
                            "    \"auth0UserId\": \"auth0|6750ff2f7016716a3ef64754\",\n" +
                            "    \"email\": \"john.doe@mail.com\",\n" +
                            "    \"name\": \"john.doe@mail.com\",\n" +
                            "    \"picture\": \"https://s.gravatar.com/avatar/f1555e33a149e074b86357032671d8a1?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fli.png\",\n" +
                            "    \"createdAt\": \"2024-12-05T01:17:35.264+00:00\"\n" +
                            "  },\n" +
                            "  \"avoidHighways\": false,\n" +
                            "  \"avoidTolls\": false,\n" +
                            "  \"preferredMode\": \"bike\",\n" +
                            "  \"ecoFriendly\": true,\n" +
                            "  \"minimizeCo2\": false,\n" +
                            "  \"avoidDangerousStreets\": true,\n" +
                            "  \"lastUpdated\": \"2024-12-02T23:34:34.068+00:00\"\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Preferences not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "404 Not Found Example",
                    value = "{\n" +
                            "  \"message\": \"Preferences not found.\"\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "500 Internal Server Error Example",
                    value = "{\n" +
                            "  \"message\": \"An error occurred: Internal server error details.\"\n" +
                            "}"
                )
            )
        )
    })
    @PostMapping("/preferences")
    public ResponseEntity<?> getUserPreferences(
            @AuthenticationPrincipal Jwt jwt) {

        try {
            String auth0UserId = jwt.getSubject();

            RoutePreference preferences = userService.getUserPreferences(auth0UserId);
            if (preferences != null) {
                return ResponseEntity.ok(preferences);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Preferences not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
