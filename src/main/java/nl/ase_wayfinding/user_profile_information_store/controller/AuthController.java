package nl.ase_wayfinding.user_profile_information_store.controller;

import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.service.Auth0TokenService;
import nl.ase_wayfinding.user_profile_information_store.service.UserService;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.time.format.DateTimeParseException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret}")
    private String clientSecret;

    @Value("${auth0.logout.return-url}")
    private String logoutReturnUrl;
    private final UserService userService;

    public AuthController(Auth0TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }
    private final Auth0TokenService tokenService;
    private final RestTemplate restTemplate = new RestTemplate();
    @Operation(
        summary = "Login User",
        description = "Logs in a user with their credentials and retrieves a token from Auth0.",
        tags = { "Authentication" }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "200 OK Example",
                    value = """
                            {
                              "auth0_user_id": "auth0|675119f9afd09e003e28439b",
                              "token_type": "Bearer",
                              "expires_in": 86400,
                              "message": "Login successful",
                              "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlRNLUVQSkd3ajJZSVY5OW0xUlJDYiJ9.eyJpc3MiOiJodHRwczovL3N1c3RhaW5hYmxlLXdheWZpbmRpbmcuZXUuYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDY3NTExOWY5YWZkMDllMDAzZTI4NDM5YiIsImF1ZCI6Imh0dHBzOi8vc3VzdGFpbmFibGUtd2F5ZmluZGluZy5ldS5hdXRoMC5jb20vYXBpL3YyLyIsImlhdCI6MTczMzM2ODM4OCwiZXhwIjoxNzMzNDU0Nzg4LCJzY29wZSI6InJlYWQ6Y3VycmVudF91c2VyIHVwZGF0ZTpjdXJyZW50X3VzZXJfbWV0YWRhdGEgZGVsZXRlOmN1cnJlbnRfdXNlcl9tZXRhZGF0YSBjcmVhdGU6Y3VycmVudF91c2VyX21ldGFkYXRhIGNyZWF0ZTpjdXJyZW50X3VzZXJfZGV2aWNlX2NyZWRlbnRpYWxzIGRlbGV0ZTpjdXJyZW50X3VzZXJfZGV2aWNlX2NyZWRlbnRpYWxzIHVwZGF0ZTpjdXJyZW50X3VzZXJfaWRlbnRpdGllcyIsImd0eSI6InBhc3N3b3JkIiwiYXpwIjoiOFdlSUZYbXNvZlhxTUYyYVNvN0RUNGROdW04RjBDRmMifQ.q0DMrlv9p_pCWKCq8Y8v0mfdILbu9hwTx-Dt1eIdyDavgnhDGDExp09j1Kb4vnlbMMege1jwLpxlwRHHuA5ySGPCh_HHYV9tBXAlBXmPsRaRDcyka-mRnYmh8OHXhkWL2W-0i6B-gvyCVVwUW49NFQiSOMhtOkCxRV0gwKX2GKwGJqr9iC6Xb-RDaIiPmCVO5nvd8TzSFd89HBTgC9PBneGJPjXBD1yuv09PfyQ2bJ65S4SFCMYnS7FhzWeLw2KEhZVSfdYl9OWC_FeD5hUSxCTO0teLibpqJoeU0k5BFCDvSLSDJFQ7MCbkj5W4vXM5QV9KhMkl9cfM8yEg-dugNQ"
                            }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "500 Internal Server Error Example",
                    value = """
                            {
                              "message": "Login failed: An unexpected error occurred",
                              "details": {
                                "error": "access_denied",
                                "error_description": "Please verify your email address before logging in."
                              }
                            }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "403 Forbidden Example",
                    value = """
                            {
                              "message": "Login failed: Authentication error",
                              "details": {
                                "error": "invalid_grant",
                                "error_description": "Wrong email or password."
                              }
                            }"""
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User credentials for login",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "Login Request Example",
                    value = """
                            {
                              "email": "john.doe@mail.com",
                              "password": "Password@123"
                            }"""
                )
            )
        )
        @RequestBody Map<String, String> credentials) {
        String url = auth0Domain + "oauth/token";

        // Prepare the request payload
        Map<String, Object> request = new HashMap<>();
        request.put("grant_type", "password");
        request.put("username", credentials.get("email"));
        request.put("password", credentials.get("password"));
        request.put("audience", auth0Domain + "api/v2/");
        request.put("client_id", clientId);
        request.put("client_secret", clientSecret);
        request.put("connection", "Username-Password-Authentication");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        try {
            // Send the request to Auth0
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Map.class);

            // Extract response details
            int statusCode = response.getStatusCode().value();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.convertValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

            String auth0UserId = (String) responseBody.get("auth0_user_id");
            String email = credentials.get("email");

            // Check if the user exists in the local database
            User user = userService.findByEmail(email);
            if (user == null) {
                // Create a new user record in the database
                user = new User();
                user.setAuth0UserId(auth0UserId);
                user.setEmail(email);
                user.setName(email); // Use email as the default name if no other info is available
                user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

                // Save the new user to the database
                userService.save(user);
            }

            // Ensure default preferences exist
            if (userService.getPreferences(user.getAuth0UserId()) == null) {
                Preferences defaultPreferences = new Preferences();
                defaultPreferences.setAuth0UserId(user.getAuth0UserId());
                defaultPreferences.setNotificationsEnabled(true);
                defaultPreferences.setTheme("light"); // default theme
                userService.savePreferences(defaultPreferences);
            }

            // Return success response with token and user details
            return ResponseEntity.status(statusCode).body(Map.of(
                "message", "Login successful",
                "token", responseBody.get("access_token"),
                "auth0_user_id", user.getAuth0UserId(),
                "expires_in", responseBody.get("expires_in"),
                "token_type", responseBody.get("token_type")
            ));
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // Handle client error exceptions and extract response details
            int errorStatusCode = ex.getStatusCode().value();
            String errorBody = ex.getResponseBodyAsString();

            // Parse the errorBody into a Map for JSON structure
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> errorDetails;
            try {
                errorDetails = objectMapper.readValue(errorBody, new TypeReference<Map<String, Object>>() {});
            } catch (Exception parseException) {
                errorDetails = Map.of("rawError", errorBody); // Fallback to raw string if parsing fails
            }

            // Return detailed error response as a clean JSON object
            return ResponseEntity.status(errorStatusCode).body(Map.of(
                "message", "Login failed: Authentication error",
                "details", errorDetails
            ));
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(500).body(Map.of(
                "message", "Login failed: An unexpected error occurred",
                "details", e.getMessage()
            ));
        }
    }



    @Operation(
        summary = "Signup User",
        description = "Registers a new user using Auth0 and saves their details in the local database.",
        tags = { "Authentication" }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Signup successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "200 OK Example",
                    value = "{\n" +
                            "  \"userId\": 3,\n" +
                            "  \"message\": \"Signup successful\",\n" +
                            "  \"auth0Response\": {\n" +
                            "    \"created_at\": \"2024-12-05T03:11:53.380Z\",\n" +
                            "    \"email\": \"john.doe@mail.com\",\n" +
                            "    \"email_verified\": false,\n" +
                            "    \"identities\": [\n" +
                            "      {\n" +
                            "        \"connection\": \"Username-Password-Authentication\",\n" +
                            "        \"user_id\": \"675119f9afd09e003e28439b\",\n" +
                            "        \"provider\": \"auth0\",\n" +
                            "        \"isSocial\": false\n" +
                            "      }\n" +
                            "    ],\n" +
                            "    \"name\": \"john.doe@mail.com\",\n" +
                            "    \"nickname\": \"john.doe\",\n" +
                            "    \"picture\": \"https://s.gravatar.com/avatar/37c39542b3174ffcaea146e9427f50ea?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fbr.png\",\n" +
                            "    \"updated_at\": \"2024-12-05T03:11:53.380Z\",\n" +
                            "    \"user_id\": \"auth0|675119f9afd09e003e28439b\"\n" +
                            "  }\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict: The user already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "409 Conflict Example",
                    value = "{\n" +
                            "  \"error\": \"Signup failed\",\n" +
                            "  \"details\": {\n" +
                            "    \"statusCode\": 409,\n" +
                            "    \"error\": \"Conflict\",\n" +
                            "    \"message\": \"The user already exists.\",\n" +
                            "    \"errorCode\": \"auth0_idp_error\"\n" +
                            "  }\n" +
                            "}"
                )
            )
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "User details for signup",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(type = "object"),
            examples = @ExampleObject(
                name = "Signup Request Example",
                value = "{\n" +
                        "  \"email\": \"john.doe@mail.com\",\n" +
                        "  \"password\": \"Password@123\"\n" +
                        "}"
            )
        )
    )    
    @RequestBody Map<String, String> userDetails) {
        String url = auth0Domain + "api/v2/users";

        // Prepare the request payload
        Map<String, Object> request = new HashMap<>();
        request.put("email", userDetails.get("email"));
        request.put("password", userDetails.get("password"));
        request.put("connection", "Username-Password-Authentication");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenService.getManagementApiToken());
        headers.set("Content-Type", "application/json");

        try {
            // Send the request to Auth0
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Map.class);

            // Extract response code and response body
            int statusCode = response.getStatusCode().value();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.convertValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

            // Log the response for debugging
            System.out.println("Auth0 Response Code: " + statusCode);
            System.out.println("Auth0 Response Body: " + responseBody);

            if (statusCode == 201 || statusCode == 200) {
                // Extract user details from Auth0 response
                String auth0UserId = (String) responseBody.get("user_id");
                String email = (String) responseBody.get("email");
                String picture = (String) responseBody.getOrDefault("picture", null);
                String createdAt = (String) responseBody.get("created_at");

                // Save user to the database
                User user = new User();
                user.setAuth0UserId(auth0UserId);
                user.setEmail(email);
                user.setName(userDetails.getOrDefault("name", email)); // Use email as fallback for name
                user.setPicture(picture);

                // Convert createdAt string to Timestamp
                try {
                    Instant instant = Instant.parse(createdAt); // Parses ISO 8601 format with 'Z'
                    Timestamp timestamp = Timestamp.from(instant);
                    user.setCreatedAt(timestamp);
                } catch (DateTimeParseException e) {
                    // Handle parsing exception
                    e.printStackTrace();
                    // You might want to set the current timestamp as a fallback
                    user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                }

                // Save the user
                userService.save(user);


                // Return successful response
                return ResponseEntity.ok(Map.of(
                    "message", "Signup successful",
                    "userId", user.getId(),
                    "auth0Response", responseBody
                ));
            } else {
                // Handle unexpected successful codes
                return ResponseEntity.status(statusCode).body(Map.of(
                    "error", "Unexpected response from Auth0",
                    "auth0Response", responseBody
                ));
            }
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            int errorStatusCode = ex.getStatusCode().value();
            String errorBody = ex.getResponseBodyAsString();

            // Parse the errorBody into a Map for JSON structure
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> errorDetails;
            try {
                errorDetails = objectMapper.readValue(errorBody, new TypeReference<Map<String, Object>>() {});
            } catch (Exception parseException) {
                errorDetails = Map.of("rawError", errorBody); // Fallback to raw string if parsing fails
            }

            // Log error details
            System.err.println("Auth0 Error Status: " + errorStatusCode);
            System.err.println("Auth0 Error Details: " + errorDetails);

            return ResponseEntity.status(errorStatusCode).body(Map.of(
                "error", "Signup failed",
                "details", errorDetails
            ));
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("Unexpected Signup Error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "An unexpected error occurred during signup",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Forgot Password",
        description = "Sends a password reset email to the user using Auth0.",
        tags = { "Authentication" }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "200 OK Example",
                    value = "{\n" +
                            "  \"message\": \"Password reset email sent\",\n" +
                            "  \"auth0Response\": \"We've just sent you an email to reset your password.\"\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "500 Internal Server Error Example",
                    value = "{\n" +
                            "  \"error\": \"An unexpected error occurred during password reset\",\n" +
                            "  \"details\": \"Detailed error message\"\n" +
                            "}"
                )
            )
        )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email address for the password reset",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "Forgot Password Request Example",
                    value = "{\n" +
                            "  \"email\": \"john.doe@mail.com\"\n" +
                            "}"
                )
            )
        )
        @RequestBody Map<String, String> requestBody) {
        String url = auth0Domain + "dbconnections/change_password";

        Map<String, Object> request = new HashMap<>();
        request.put("client_id", clientId);
        request.put("email", requestBody.get("email"));
        request.put("connection", "Username-Password-Authentication");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        try {
            // Send the request to Auth0
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class
            );

            // Extract response status code and body
            int statusCode = response.getStatusCode().value();
            String responseBody = response.getBody();

            // Return response with Auth0 details
            return ResponseEntity.status(statusCode).body(Map.of(
                "message", "Password reset email sent",
                "auth0Response", responseBody
            ));
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // Handle client error exceptions
            int errorStatusCode = ex.getStatusCode().value();
            String errorBody = ex.getResponseBodyAsString();

            // Parse errorBody if possible, fallback to raw text
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> errorDetails;
            try {
                errorDetails = objectMapper.readValue(errorBody, new TypeReference<Map<String, Object>>() {});
            } catch (Exception parseException) {
                errorDetails = Map.of("rawError", errorBody); // Fallback to raw string
            }

            // Return error response
            return ResponseEntity.status(errorStatusCode).body(Map.of(
                "error", "Password reset failed",
                "details", errorDetails
            ));
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(500).body(Map.of(
                "error", "An unexpected error occurred during password reset",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Logout User",
        description = "Logs out the user by invalidating the session in Auth0 and redirects to the return URL.",
        tags = { "Authentication" }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "302 Found Example",
                    value = "{\n" +
                            "  \"message\": \"Logout successful\",\n" +
                            "  \"auth0Response\": \"Found. Redirecting to http://localhost:8080\"\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object"),
                examples = @ExampleObject(
                    name = "500 Internal Server Error Example",
                    value = "{\n" +
                            "  \"error\": \"An unexpected error occurred during logout\",\n" +
                            "  \"details\": \"Detailed error message\"\n" +
                            "}"
                )
            )
        )
    })
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        String logoutUrl = auth0Domain + "v2/logout" +
                "?client_id=" + clientId;

        try {
            // Send the request to Auth0
            ResponseEntity<String> response = restTemplate.getForEntity(logoutUrl, String.class);

            // Extract response details
            int statusCode = response.getStatusCode().value();
            String responseBody = response.getBody();

            // Return successful logout response
            return ResponseEntity.status(statusCode).body(Map.of(
                "message", "Logout successful",
                "auth0Response", responseBody
            ));
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // Handle client error exceptions and extract response details
            int errorStatusCode = ex.getStatusCode().value();
            String errorBody = ex.getResponseBodyAsString();

            // Parse the errorBody into a Map for JSON structure
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> errorDetails;
            try {
                errorDetails = objectMapper.readValue(errorBody, new TypeReference<Map<String, Object>>() {});
            } catch (Exception parseException) {
                errorDetails = Map.of("rawError", errorBody); // Fallback to raw string if parsing fails
            }

            // Return error response
            return ResponseEntity.status(errorStatusCode).body(Map.of(
                "error", "Logout failed",
                "details", errorDetails
            ));
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(500).body(Map.of(
                "error", "An unexpected error occurred during logout",
                "details", e.getMessage()
            ));
        }
    }

}
