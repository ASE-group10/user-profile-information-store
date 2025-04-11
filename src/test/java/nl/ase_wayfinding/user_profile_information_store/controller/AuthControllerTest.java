package nl.ase_wayfinding.user_profile_information_store.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.service.Auth0TokenService;
import nl.ase_wayfinding.user_profile_information_store.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

        @Mock
        private UserService userService;

        @Mock
        private Auth0TokenService tokenService;

        @Mock
        private RestTemplate restTemplate;

        @InjectMocks
        private AuthController authController;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        public void setup() {
                MockitoAnnotations.openMocks(this);

                // Create a new controller instance and inject the RestTemplate directly
                authController = new AuthController(tokenService, userService);
                ReflectionTestUtils.setField(authController, "restTemplate", restTemplate);

                // Set required Auth0 properties
                ReflectionTestUtils.setField(authController, "auth0Domain", "https://test-domain.auth0.com/");
                ReflectionTestUtils.setField(authController, "clientId", "test-client-id");
                ReflectionTestUtils.setField(authController, "clientSecret", "test-client-secret");
                ReflectionTestUtils.setField(authController, "logoutReturnUrl", "http://localhost:8080");
        }

        @Test
        public void testLogin_Success() {
                // Arrange
                Map<String, String> credentials = new HashMap<>();
                credentials.put("email", "test@example.com");
                credentials.put("password", "Password123");

                // Mock Auth0 token response
                Map<String, Object> tokenResponse = new HashMap<>();
                tokenResponse.put("access_token", "test-access-token");
                tokenResponse.put("expires_in", 86400);
                tokenResponse.put("token_type", "Bearer");

                ResponseEntity<Map> tokenResponseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
                when(restTemplate.postForEntity(contains("oauth/token"), any(HttpEntity.class), eq(Map.class)))
                                .thenReturn(tokenResponseEntity);

                // Mock Auth0 userinfo response
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("sub", "auth0|123456789");
                ResponseEntity<Map> userInfoResponseEntity = new ResponseEntity<>(userInfo, HttpStatus.OK);
                when(restTemplate.exchange(
                                contains("userinfo"),
                                eq(HttpMethod.GET),
                                any(HttpEntity.class),
                                eq(Map.class)))
                                .thenReturn(userInfoResponseEntity);

                // Mock existing user
                User existingUser = new User();
                existingUser.setAuth0UserId("auth0|123456789");
                existingUser.setEmail("test@example.com");
                when(userService.findByEmail("test@example.com")).thenReturn(existingUser);

                // Mock existing preferences
                Preferences existingPreferences = new Preferences();
                when(userService.getPreferences("auth0|123456789")).thenReturn(existingPreferences);

                // Act
                ResponseEntity<?> response = authController.login(credentials);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Login successful", responseBody.get("message"));
                assertEquals("test-access-token", responseBody.get("token"));
                assertEquals("auth0|123456789", responseBody.get("auth0_user_id"));

                // Verify userService was called to find the user
                verify(userService, times(1)).findByEmail("test@example.com");
                verify(userService, times(1)).getPreferences("auth0|123456789");
        }

        @Test
        public void testLogin_NewUser_Success() {
                // Arrange
                Map<String, String> credentials = new HashMap<>();
                credentials.put("email", "newuser@example.com");
                credentials.put("password", "Password123");

                // Mock Auth0 token response
                Map<String, Object> tokenResponse = new HashMap<>();
                tokenResponse.put("access_token", "test-access-token");
                tokenResponse.put("expires_in", 86400);
                tokenResponse.put("token_type", "Bearer");

                ResponseEntity<Map> tokenResponseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
                when(restTemplate.postForEntity(contains("oauth/token"), any(HttpEntity.class), eq(Map.class)))
                                .thenReturn(tokenResponseEntity);

                // Mock Auth0 userinfo response
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("sub", "auth0|987654321");
                ResponseEntity<Map> userInfoResponseEntity = new ResponseEntity<>(userInfo, HttpStatus.OK);
                when(restTemplate.exchange(
                                contains("userinfo"),
                                eq(HttpMethod.GET),
                                any(HttpEntity.class),
                                eq(Map.class)))
                                .thenReturn(userInfoResponseEntity);

                // Mock no existing user (new user)
                when(userService.findByEmail("newuser@example.com")).thenReturn(null);

                // Mock no existing preferences
                when(userService.getPreferences("auth0|987654321")).thenReturn(null);

                // Act
                ResponseEntity<?> response = authController.login(credentials);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Login successful", responseBody.get("message"));

                // Verify userService was called to save the new user and preferences
                verify(userService, times(1)).findByEmail("newuser@example.com");
                verify(userService, times(1)).save(any(User.class));
                verify(userService, times(1)).savePreferences(any(Preferences.class));
        }

        @Test
        public void testLogin_Auth0Error() {
                // Arrange
                Map<String, String> credentials = new HashMap<>();
                credentials.put("email", "test@example.com");
                credentials.put("password", "WrongPassword");

                // Mock Auth0 error response
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "invalid_grant");
                errorResponse.put("error_description", "Wrong email or password.");

                String errorBodyJson = "{\"error\":\"invalid_grant\",\"error_description\":\"Wrong email or password.\"}";
                HttpClientErrorException exception = HttpClientErrorException.create(
                                HttpStatus.FORBIDDEN,
                                "Forbidden",
                                null,
                                errorBodyJson.getBytes(),
                                null);

                when(restTemplate.postForEntity(contains("oauth/token"), any(HttpEntity.class), eq(Map.class)))
                                .thenThrow(exception);

                // Act
                ResponseEntity<?> response = authController.login(credentials);

                // Assert
                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Login failed: Authentication error", responseBody.get("message"));

                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) responseBody.get("details");
                assertEquals("invalid_grant", details.get("error"));
        }

        @Test
        public void testSignup_Success() {
                // Arrange
                Map<String, String> userDetails = new HashMap<>();
                userDetails.put("email", "newuser@example.com");
                userDetails.put("password", "Password123");
                userDetails.put("name", "New User");
                userDetails.put("phoneNumber", "+12345678901");

                // Mock Auth0 token service
                when(tokenService.getManagementApiToken()).thenReturn("test-management-token");

                // Mock Auth0 signup response
                Map<String, Object> signupResponse = new HashMap<>();
                signupResponse.put("user_id", "auth0|123456789");
                signupResponse.put("email", "newuser@example.com");
                signupResponse.put("created_at", "2023-04-11T12:00:00.000Z");
                signupResponse.put("picture", "https://example.com/avatar.png");

                ResponseEntity<Map> signupResponseEntity = new ResponseEntity<>(signupResponse, HttpStatus.CREATED);
                when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                                .thenReturn(signupResponseEntity);

                // Ensure the User object is properly mocked for return
                User newUser = new User();
                newUser.setAuth0UserId("auth0|123456789");
                newUser.setEmail("newuser@example.com");

                // Use doReturn() for void methods or when you need to chain mocks
                doReturn(newUser).when(userService).save(any(User.class));
                // Or if save() returns the saved entity:
                // when(userService.save(any(User.class))).thenReturn(newUser);

                // Act
                ResponseEntity<?> response = authController.signup(userDetails);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Signup successful", responseBody.get("message"));

                // Verify user was saved
                verify(userService, times(1)).save(any(User.class));
        }

        @Test
        public void testSignup_UserExists() {
                // Arrange
                Map<String, String> userDetails = new HashMap<>();
                userDetails.put("email", "existing@example.com");
                userDetails.put("password", "Password123");

                // Mock Auth0 token service
                when(tokenService.getManagementApiToken()).thenReturn("test-management-token");

                // Mock Auth0 error response for existing user
                String errorBodyJson = "{\"statusCode\":409,\"error\":\"Conflict\",\"message\":\"The user already exists.\",\"errorCode\":\"auth0_idp_error\"}";
                HttpClientErrorException exception = HttpClientErrorException.create(
                                HttpStatus.CONFLICT,
                                "Conflict",
                                null,
                                errorBodyJson.getBytes(),
                                null);

                when(restTemplate.postForEntity(contains("api/v2/users"), any(HttpEntity.class), eq(Map.class)))
                                .thenThrow(exception);

                // Act
                ResponseEntity<?> response = authController.signup(userDetails);

                // Assert
                assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Signup failed", responseBody.get("error"));

                // Verify user was not saved
                verify(userService, never()).save(any(User.class));
        }

        @Test
        public void testForgotPassword_Success() {
                // Arrange
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("email", "user@example.com");

                // Mock Auth0 password reset response
                ResponseEntity<String> passwordResetResponse = new ResponseEntity<>(
                                "We've just sent you an email to reset your password.", HttpStatus.OK);
                when(restTemplate.exchange(
                                contains("dbconnections/change_password"),
                                eq(HttpMethod.POST),
                                any(HttpEntity.class),
                                eq(String.class)))
                                .thenReturn(passwordResetResponse);

                // Act
                ResponseEntity<?> response = authController.forgotPassword(requestBody);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Password reset email sent", responseBody.get("message"));
                assertEquals("We've just sent you an email to reset your password.", responseBody.get("auth0Response"));
        }

        @Test
        public void testForgotPassword_UserNotFound() {
                // Arrange
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("email", "nonexistent@example.com");

                // Mock Auth0 error response
                String errorBodyJson = "{\"error\":\"user_not_found\",\"error_description\":\"User not found\"}";
                HttpClientErrorException exception = HttpClientErrorException.create(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                null,
                                errorBodyJson.getBytes(),
                                null);

                when(restTemplate.exchange(
                                contains("dbconnections/change_password"),
                                eq(HttpMethod.POST),
                                any(HttpEntity.class),
                                eq(String.class)))
                                .thenThrow(exception);

                // Act
                ResponseEntity<?> response = authController.forgotPassword(requestBody);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Password reset failed", responseBody.get("error"));
        }

        @Test
        public void testLogout_Success() {
                // Arrange
                ResponseEntity<String> logoutResponse = new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
                when(restTemplate.getForEntity(contains("v2/logout"), eq(String.class)))
                                .thenReturn(logoutResponse);

                // Act
                ResponseEntity<?> response = authController.logout();

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());

                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                assertEquals("Logout successful", responseBody.get("message"));
                assertEquals("Logged out successfully", responseBody.get("auth0Response"));
        }
}