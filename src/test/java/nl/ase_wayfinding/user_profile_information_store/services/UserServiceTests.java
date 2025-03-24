package nl.ase_wayfinding.user_profile_information_store.service;

import nl.ase_wayfinding.user_profile_information_store.model.RoutePreference;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.repository.RoutePreferenceRepository;
import nl.ase_wayfinding.user_profile_information_store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoutePreferenceRepository preferenceRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RoutePreference testPreference;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setAuth0UserId("auth0|123456789");

        testPreference = new RoutePreference();
        testPreference.setPreferenceId(1L);
        testPreference.setUser(testUser);
        testPreference.setAvoidHighways(true);
        testPreference.setAvoidTolls(true);
        testPreference.setPreferredMode("bicycle");
        testPreference.setEcoFriendly(true);
        testPreference.setMinimizeCo2(false);
        testPreference.setAvoidDangerousStreets(true);
        testPreference.setLastUpdated(Timestamp.from(Instant.now()));
    }

    @Test
    void testSave() {
        // Setup
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Execute
        userService.save(testUser);

        // Verify
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testFindByEmail_UserExists() {
        // Setup
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Execute
        User foundUser = userService.findByEmail("test@example.com");

        // Verify
        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testFindByEmail_UserDoesNotExist() {
        // Setup
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Execute
        User foundUser = userService.findByEmail("nonexistent@example.com");

        // Verify
        assertNull(foundUser);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testGetUserPreferences_UserAndPreferencesExist() {
        // Setup
        when(userRepository.findByAuth0UserId("auth0|123456789")).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.of(testPreference));

        // Execute
        RoutePreference foundPreference = userService.getUserPreferences("auth0|123456789");

        // Verify
        assertNotNull(foundPreference);
        assertEquals(1L, foundPreference.getPreferenceId());
        assertTrue(foundPreference.getAvoidHighways());
        assertTrue(foundPreference.getAvoidTolls());
        assertEquals("bicycle", foundPreference.getPreferredMode());
        assertTrue(foundPreference.getEcoFriendly());
        assertFalse(foundPreference.getMinimizeCo2());
        assertTrue(foundPreference.getAvoidDangerousStreets());
        assertNotNull(foundPreference.getLastUpdated());
        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456789");
        verify(preferenceRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetUserPreferences_UserExistsButNoPreferences() {
        // Setup
        when(userRepository.findByAuth0UserId("auth0|123456789")).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // Execute
        RoutePreference foundPreference = userService.getUserPreferences("auth0|123456789");

        // Verify
        assertNull(foundPreference);
        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456789");
        verify(preferenceRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetUserPreferences_UserDoesNotExist() {
        // Setup
        when(userRepository.findByAuth0UserId("nonexistent")).thenReturn(Optional.empty());

        // Execute
        RoutePreference foundPreference = userService.getUserPreferences("nonexistent");

        // Verify
        assertNull(foundPreference);
        verify(userRepository, times(1)).findByAuth0UserId("nonexistent");
        verify(preferenceRepository, never()).findByUser(any());
    }
}