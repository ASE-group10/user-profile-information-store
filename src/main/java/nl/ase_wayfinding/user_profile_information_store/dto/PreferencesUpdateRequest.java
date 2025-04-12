package nl.ase_wayfinding.user_profile_information_store.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class PreferencesUpdateRequest {
    private boolean notificationsEnabled;
    private String theme; // "light", "dark"
    private String language; // Added to match test expectations
}
