package nl.ase_wayfinding.user_profile_information_store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "preferences")
public class Preferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String auth0UserId;

    // Adding userId field to match test expectations
    private String userId;

    private boolean notificationsEnabled;

    // Adding field to maintain backward compatibility
    private boolean notificationEnabled;

    // Adding language field to match test expectations
    private String language;

    private String theme; // e.g., "dark", "light"

    // Add setter methods that tests are expecting
    public void setNotificationEnabled(boolean enabled) {
        this.notificationEnabled = enabled;
        this.notificationsEnabled = enabled; // Keep both fields in sync
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.auth0UserId = userId; // Keep both fields in sync
    }
}
