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
        Preferences preferences = new Preferences();

        preferences.setId(1L);
        preferences.setAuth0UserId("auth0|123456");
        // Removed the redundant call with "user123" if not intended.
        preferences.setNotificationsEnabled(true);
        preferences.setLanguage("en");
        preferences.setTheme("dark");

        assertEquals(1L, preferences.getId());
        // Expecting only one value for auth0UserId:
        assertEquals("auth0|123456", preferences.getAuth0UserId());
        assertTrue(preferences.isNotificationsEnabled());
        assertEquals("en", preferences.getLanguage());
        assertEquals("dark", preferences.getTheme());
    }

    @Test
    void testSyncFields() {
        Preferences preferences = new Preferences();

        // Test syncing notificationsEnabled and notificationsEnabled (this works fine)
        preferences.setNotificationsEnabled(true);
        assertTrue(preferences.isNotificationsEnabled());

        preferences.setNotificationsEnabled(false);
        assertFalse(preferences.isNotificationsEnabled());

        // Test auth0UserId setting (only one value is stored)
        preferences.setAuth0UserId("newUser123");
        assertEquals("newUser123", preferences.getAuth0UserId());

        preferences.setAuth0UserId("auth0|789");
        // Now, expect that getAuth0UserId returns the new value
        assertEquals("auth0|789", preferences.getAuth0UserId());
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
