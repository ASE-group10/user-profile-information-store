package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PreferencesTest {

    @Test
    void testSyncUserIdAuth0UserId() {
        Preferences pref = new Preferences();
        pref.setAuth0UserId("user123");
        // Setter should sync both fields
        assertEquals("user123", pref.getAuth0UserId());
        assertEquals("user123", pref.getAuth0UserId());
    }

    @Test
    void testNotificationSync() {
        Preferences pref = new Preferences();
        pref.setNotificationsEnabled(true);
        assertTrue(pref.isNotificationsEnabled());
    }

    @Test
    void testPreferencesGettersAndSetters() {
        // Create a preferences object
        Preferences preferences = new Preferences();

        // Set values
        preferences.setId(1L);
        preferences.setAuth0UserId("auth0|123456");
        preferences.setAuth0UserId("user123");
        preferences.setNotificationsEnabled(true);
        preferences.setLanguage("en");
        preferences.setTheme("dark");

        // Test getters
        assertEquals(1L, preferences.getId());
        assertEquals("auth0|123456", preferences.getAuth0UserId());
        assertEquals("user123", preferences.getAuth0UserId());
        assertTrue(preferences.isNotificationsEnabled());
        assertEquals("en", preferences.getLanguage());
        assertEquals("dark", preferences.getTheme());
    }

    @Test
    void testSyncFields() {
        Preferences preferences = new Preferences();

        // Test syncing notificationEnabled and notificationsEnabled
        preferences.setNotificationsEnabled(true);
        assertTrue(preferences.isNotificationsEnabled());

        preferences.setNotificationsEnabled(false);
        assertFalse(preferences.isNotificationsEnabled());

        // Test syncing userId and auth0UserId
        preferences.setAuth0UserId("newUser123");
        assertEquals("newUser123", preferences.getAuth0UserId());
        assertEquals("newUser123", preferences.getAuth0UserId());

        preferences.setAuth0UserId("auth0|789");
        assertEquals("auth0|789", preferences.getAuth0UserId());
        // This doesn't auto-sync when setting auth0UserId directly
        assertEquals("newUser123", preferences.getAuth0UserId());
    }

    @Test
    void testDefaultValues() {
        Preferences preferences = new Preferences();

        // Test default values
        assertNull(preferences.getId());
        assertNull(preferences.getAuth0UserId());
        assertFalse(preferences.isNotificationsEnabled());
        assertNull(preferences.getLanguage());
        assertNull(preferences.getTheme());
    }
}
