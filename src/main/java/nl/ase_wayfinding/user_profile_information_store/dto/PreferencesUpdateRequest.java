package nl.ase_wayfinding.user_profile_information_store.dto;

import lombok.Data;

@Data
public class PreferencesUpdateRequest {
    private boolean notificationsEnabled;
    private boolean notificationEnabled; // Added to match test expectations
    private String theme; // "light", "dark"
    private String language; // Added to match test expectations

    // Add methods that tests are expecting
    public void setNotificationEnabled(boolean enabled) {
        this.notificationEnabled = enabled;
        this.notificationsEnabled = enabled; // Keep both fields in sync
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
