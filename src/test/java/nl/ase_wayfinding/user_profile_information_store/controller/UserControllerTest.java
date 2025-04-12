package nl.ase_wayfinding.user_profile_information_store.controller;

import nl.ase_wayfinding.user_profile_information_store.dto.PreferencesUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UserController userController;

    private final String testUserId = "auth0|123456789";
    private User testUser;
    private Preferences testPreferences;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Set up test JWT
        when(jwt.getSubject()).thenReturn(testUserId);

        // Set up test user data
        testUser = new User();
        testUser.setAuth0UserId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Set up test preferences
        testPreferences = new Preferences();
        testPreferences.setAuth0UserId(testUserId);
        testPreferences.setNotificationsEnabled(true);
        testPreferences.setLanguage("en");
        testPreferences.setTheme("light");
    }

    @Test
    public void testGetPreferences_Success() {
        // Arrange
        when(userService.getUserPreferences(testUserId)).thenReturn(Optional.of(testPreferences));

        // Act
        ResponseEntity<?> response = userController.getPreferences(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPreferences, response.getBody());
        verify(userService, times(1)).getUserPreferences(testUserId);
    }

    @Test
    public void testGetPreferences_NotFound() {
        // Arrange
        when(userService.getUserPreferences(testUserId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.getPreferences(jwt);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).getUserPreferences(testUserId);
    }

    @Test
    public void testGetAccountInfo_Success() {
        // Arrange
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = userController.getAccountInfo(jwt);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService, times(1)).getUserById(testUserId);
    }

    @Test
    public void testGetAccountInfo_NotFound() {
        // Arrange
        when(userService.getUserById(testUserId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.getAccountInfo(jwt);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).getUserById(testUserId);
    }

    @Test
    public void testUpdatePreferences_Success() {
        // Arrange
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setNotificationsEnabled(false);
        request.setLanguage("fr");
        request.setTheme("dark");

        doNothing().when(userService).updatePreferences(eq(testUserId), any(PreferencesUpdateRequest.class));

        // Act
        ResponseEntity<?> response = userController.updatePreferences(jwt, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("successfully"));
        verify(userService, times(1)).updatePreferences(eq(testUserId), any(PreferencesUpdateRequest.class));
    }

    @Test
    public void testUpdatePreferences_Error() {
        // Arrange
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setNotificationsEnabled(false);

        doThrow(new RuntimeException("Database error")).when(userService).updatePreferences(eq(testUserId),
                any(PreferencesUpdateRequest.class));

        // Act
        ResponseEntity<?> response = userController.updatePreferences(jwt, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to update preferences"));
        verify(userService, times(1)).updatePreferences(eq(testUserId), any(PreferencesUpdateRequest.class));
    }

    @Test
    public void testGetPreferences_NullJwt() {
        // Act
        ResponseEntity<?> response = userController.getPreferences(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized"));
    }

    @Test
    public void testGetAccountInfo_NullJwt() {
        // Act
        ResponseEntity<?> response = userController.getAccountInfo(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized"));
    }

    @Test
    public void testUpdatePreferences_NullJwt() {
        // Arrange
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();

        // Act
        ResponseEntity<?> response = userController.updatePreferences(null, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized"));
    }

    @Test
    public void testUpdatePreferences_AllFields() {
        // Arrange
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setNotificationsEnabled(false);
        request.setLanguage("es");
        request.setTheme("dark");

        doNothing().when(userService).updatePreferences(eq(testUserId), any(PreferencesUpdateRequest.class));

        // Act
        ResponseEntity<?> response = userController.updatePreferences(jwt, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updatePreferences(eq(testUserId), any(PreferencesUpdateRequest.class));
    }

    // Removing updateAccount tests as this method doesn't exist in UserController
}