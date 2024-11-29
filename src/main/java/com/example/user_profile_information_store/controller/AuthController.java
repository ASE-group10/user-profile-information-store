package com.example.user_profile_information_store.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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

    @Value("${auth0.management.api.token}")
    private String managementApiToken;

    @Value("${auth0.logout.return-url}")
    private String logoutReturnUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String url = auth0Domain + "/oauth/token";

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
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> userDetails) {
        String url = auth0Domain + "/api/v2/users";

        Map<String, Object> request = new HashMap<>();
        request.put("email", userDetails.get("email"));
        request.put("password", userDetails.get("password"));
        request.put("connection", "Username-Password-Authentication");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + managementApiToken);
        headers.set("Content-Type", "application/json");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Signup failed: " + e.getMessage());
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
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
            return ResponseEntity.ok("Password reset email sent to " + requestBody.get("email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send password reset email: " + e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        try {
            String logoutUrl = auth0Domain + "/v2/logout" +
                    "?client_id=" + clientId +
                    "&returnTo=" + logoutReturnUrl;

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(logoutUrl, String.class);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Logout failed",
                "error", e.getMessage()
            ));
        }
    }

}
