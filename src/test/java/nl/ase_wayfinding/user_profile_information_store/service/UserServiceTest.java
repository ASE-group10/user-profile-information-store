package nl.ase_wayfinding.user_profile_information_store.service;

import nl.ase_wayfinding.user_profile_information_store.dto.AccountUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.PreferencesUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.repository.PreferencesRepository;
import nl.ase_wayfinding.user_profile_information_store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PreferencesRepository preferencesRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Preferences testPreferences;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up test user
        testUser = new User();
        testUser.setAuth0Id("auth0|123456");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Set up test preferences
        testPreferences = new Preferences();
        testPreferences.setUserId("auth0|123456");
        testPreferences.setAuth0UserId("auth0|123456");
        testPreferences.setNotificationsEnabled(true);
        testPreferences.setLanguage("en");
        testPreferences.setTheme("light");
    }

    @Test
    void testFindByEmail() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());

        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        User result = userService.findByEmail("nonexistent@example.com");

        // Assert
        assertNull(result);

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testSave() {
        // Arrange
        doNothing().when(userRepository).save(any(User.class));

        // Act
        userService.save(testUser);

        // Assert
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testGetPreferences() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testUser));
        when(preferencesRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testPreferences));

        // Act
        Preferences result = userService.getPreferences("auth0|123456");

        // Assert
        assertNotNull(result);
        assertEquals("auth0|123456", result.getAuth0UserId());
        assertEquals("en", result.getLanguage());

        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(preferencesRepository, times(1)).findByAuth0UserId("auth0|123456");
    }

    @Test
    void testGetPreferences_UserNotFound() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|nonexistent")).thenReturn(Optional.empty());

        // Act
        Preferences result = userService.getPreferences("auth0|nonexistent");

        // Assert
        assertNull(result);

        verify(userRepository, times(1)).findByAuth0UserId("auth0|nonexistent");
        verify(preferencesRepository, never()).findByAuth0UserId(anyString());
    }

    @Test
    void testGetPreferences_PreferencesNotFound() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testUser));
        when(preferencesRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.empty());

        // Act
        Preferences result = userService.getPreferences("auth0|123456");

        // Assert
        assertNull(result);

        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(preferencesRepository, times(1)).findByAuth0UserId("auth0|123456");
    }

    @Test
    void testGetUserPreferences() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testUser));
        when(preferencesRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testPreferences));

        // Act
        Optional<Preferences> result = userService.getUserPreferences("auth0|123456");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("auth0|123456", result.get().getAuth0UserId());

        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(preferencesRepository, times(1)).findByAuth0UserId("auth0|123456");
    }

    @Test
    void testGetUserPreferences_NotFound() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<Preferences> result = userService.getUserPreferences("auth0|nonexistent");

        // Assert
        assertFalse(result.isPresent());

        verify(userRepository, times(1)).findByAuth0UserId("auth0|nonexistent");
    }

    @Test
    void testSavePreferences() {
        // Arrange
        doNothing().when(preferencesRepository).save(any(Preferences.class));

        // Act
        userService.savePreferences(testPreferences);

        // Assert
        verify(preferencesRepository, times(1)).save(testPreferences);
    }

    @Test
    void testGetUserById() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById("auth0|123456");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());

        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456");
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findByAuth0UserId("auth0|nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById("auth0|nonexistent");

        // Assert
        assertFalse(result.isPresent());

        verify(userRepository, times(1)).findByAuth0UserId("auth0|nonexistent");
    }

    @Test
    void testUpdatePreferences() {
        // Arrange
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setNotificationEnabled(false);
        request.setLanguage("fr");
        request.setTheme("dark");

        when(preferencesRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testPreferences));
        doNothing().when(preferencesRepository).save(any(Preferences.class));

        // Act
        userService.updatePreferences("auth0|123456", request);

        // Assert
        verify(preferencesRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(preferencesRepository, times(1)).save(any(Preferences.class));
    }

    @Test
    void testUpdatePreferences_CreateNew() {
        // Arrange
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setNotificationEnabled(false);

        when(preferencesRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.empty());
        doNothing().when(preferencesRepository).save(any(Preferences.class));

        // Act
        userService.updatePreferences("auth0|123456", request);

        // Assert
        verify(preferencesRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(preferencesRepository, times(1)).save(any(Preferences.class));
    }

    @Test
    void testUpdateAccount() {
        // Arrange
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName("Updated Name");
        request.setPicture("new-picture-url");
        request.setPhoneNumber("+1234567890");

        when(userRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).save(any(User.class));

        // Act
        userService.updateAccount("auth0|123456", request);

        // Assert
        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("Updated Name", testUser.getName());
        assertEquals("new-picture-url", testUser.getPicture());
        assertEquals("+1234567890", testUser.getPhoneNumber());
    }

    @Test
    void testUpdateAccount_PartialUpdate() {
        // Arrange
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName("Updated Name");
        // Not updating picture or phone number

        when(userRepository.findByAuth0UserId("auth0|123456")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).save(any(User.class));

        // Act
        userService.updateAccount("auth0|123456", request);

        // Assert
        verify(userRepository, times(1)).findByAuth0UserId("auth0|123456");
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("Updated Name", testUser.getName());
    }

    @Test
    void testUpdateAccount_UserNotFound() {
        // Arrange
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName("Updated Name");

        when(userRepository.findByAuth0UserId("auth0|nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.updateAccount("auth0|nonexistent", request));

        verify(userRepository, times(1)).findByAuth0UserId("auth0|nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }
}
