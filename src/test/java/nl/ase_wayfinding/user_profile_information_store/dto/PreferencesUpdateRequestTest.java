package nl.ase_wayfinding.user_profile_information_store.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PreferencesUpdateRequestTest {

    @Test
    void testNotificationSync() {
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setNotificationsEnabled(true);

        assertTrue(request.isNotificationsEnabled());

        // Change notificationsEnabled only through setter (if available)
        request.setNotificationsEnabled(false);
        assertFalse(request.isNotificationsEnabled());
    }

    @Test
    void testThemeAndLanguage() {
        PreferencesUpdateRequest request = new PreferencesUpdateRequest();
        request.setTheme("dark");
        request.setLanguage("en");

        assertEquals("dark", request.getTheme());
        assertEquals("en", request.getLanguage());
    }
}
