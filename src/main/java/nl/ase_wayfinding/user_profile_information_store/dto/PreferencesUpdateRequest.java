package nl.ase_wayfinding.user_profile_information_store.dto;

import lombok.Data;

@Data
public class PreferencesUpdateRequest {
    private boolean notificationsEnabled;
    private String theme; // "light", "dark"
}
