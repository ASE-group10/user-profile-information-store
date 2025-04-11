package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PreferencesTest {

    @Test
    void testSyncUserIdAuth0UserId() {
        Preferences pref = new Preferences();
        pref.setUserId("user123");
        // Setter should sync both fields
        assertEquals("user123", pref.getUserId());
        assertEquals("user123", pref.getAuth0UserId());
    }

    @Test
    void testNotificationSync() {
        Preferences pref = new Preferences();
        pref.setNotificationEnabled(true);
        assertTrue(pref.isNotificationEnabled());
        // The two fields must be in sync.
        assertTrue(pref.isNotificationsEnabled());
    }

    @Test
    void testPreferencesGettersAndSetters() {
        // Create a preferences object
        Preferences preferences = new Preferences();

        // Set values
        preferences.setId(1L);
        preferences.setAuth0UserId("auth0|123456");
        preferences.setUserId("user123");
        preferences.setNotificationsEnabled(true);
        preferences.setNotificationEnabled(false); // This should be synced
        preferences.setLanguage("en");
        preferences.setTheme("dark");

        // Test getters
        assertEquals(1L, preferences.getId());
        assertEquals("auth0|123456", preferences.getAuth0UserId());
        assertEquals("user123", preferences.getUserId());
        assertTrue(preferences.isNotificationsEnabled());
        assertFalse(preferences.isNotificationEnabled()); // This should be synced with notificationsEnabled
        assertEquals("en", preferences.getLanguage());
        assertEquals("dark", preferences.getTheme());
    }

    @Test
    void testSyncFields() {
        Preferences preferences = new Preferences();

        // Test syncing notificationEnabled and notificationsEnabled
        preferences.setNotificationEnabled(true);
        assertTrue(preferences.isNotificationsEnabled());
        assertTrue(preferences.isNotificationEnabled());

        preferences.setNotificationsEnabled(false);
        assertFalse(preferences.isNotificationsEnabled());
        // This doesn't auto-sync when setting notificationsEnabled directly
        assertTrue(preferences.isNotificationEnabled());

        // Test syncing userId and auth0UserId
        preferences.setUserId("newUser123");
        assertEquals("newUser123", preferences.getUserId());
        assertEquals("newUser123", preferences.getAuth0UserId());

        preferences.setAuth0UserId("auth0|789");
        assertEquals("auth0|789", preferences.getAuth0UserId());
        // This doesn't auto-sync when setting auth0UserId directly
        assertEquals("newUser123", preferences.getUserId());
    }

    @Test
    void testDefaultValues() {
        Preferences preferences = new Preferences();

        // Test default values
        assertNull(preferences.getId());
        assertNull(preferences.getAuth0UserId());
        assertNull(preferences.getUserId());
        assertFalse(preferences.isNotificationsEnabled());
        assertFalse(preferences.isNotificationEnabled());
        assertNull(preferences.getLanguage());
        assertNull(preferences.getTheme());
    }
}
