package com.example.user_profile_information_store.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Auth0TokenService {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret}")
    private String clientSecret;

    private String managementApiToken;
    private long tokenExpirationTime;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getManagementApiToken() {
        if (managementApiToken == null || System.currentTimeMillis() > tokenExpirationTime) {
            fetchManagementApiToken();
        }
        return managementApiToken;
    }

    private void fetchManagementApiToken() {
        String url = auth0Domain + "/oauth/token";

        Map<String, Object> request = new HashMap<>();
        request.put("client_id", clientId);
        request.put("client_secret", clientSecret);
        request.put("audience", auth0Domain + "/api/v2/");
        request.put("grant_type", "client_credentials");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Map.class);
            managementApiToken = (String) response.getBody().get("access_token");
            int expiresIn = (Integer) response.getBody().get("expires_in");
            tokenExpirationTime = System.currentTimeMillis() + expiresIn * 1000L;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch management API token: " + e.getMessage());
        }
    }
}
