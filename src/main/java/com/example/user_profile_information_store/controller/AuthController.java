package com.example.user_profile_information_store.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.user_profile_information_store.service.Auth0TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.user_profile_information_store.model.User;
import com.example.user_profile_information_store.service.UserService;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret}")
    private String clientSecret;

    @Value("${auth0.management.api.token}")
    private String managementApiToken;

    @Value("${auth0.logout.return-url}")
    private String logoutReturnUrl;
    private final UserService userService;

    public AuthController(Auth0TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }
    private final Auth0TokenService tokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String url = auth0Domain + "/oauth/token";

        // Prepare the request payload
        Map<String, Object> request = new HashMap<>();
        request.put("grant_type", "password");
        request.put("username", credentials.get("email"));
        request.put("password", credentials.get("password"));
        request.put("audience", auth0Domain + "/api/v2/");
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

            // Check if the user exists in the local database
            User user = userService.findByEmail(credentials.get("email"));
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not found in the local database",
                    "auth0Response", responseBody
                ));
            }

            // Return the token and auth0_user_id
            return ResponseEntity.status(statusCode).body(Map.of(
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

            // Return error response
            return ResponseEntity.status(errorStatusCode).body(Map.of(
                "error", "Login failed",
                "details", errorDetails
            ));
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(500).body(Map.of(
                "error", "An unexpected error occurred during login",
                "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> userDetails) {
        String url = auth0Domain + "/api/v2/users";

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> requestBody) {
        String url = auth0Domain + "/dbconnections/change_password";

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


    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        String logoutUrl = auth0Domain + "/v2/logout" +
                "?client_id=" + clientId +
                "&returnTo=" + logoutReturnUrl;

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
