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

    // Mark this field as transient so it is not mapped to a separate column.
    @Transient
    private boolean notificationEnabled;

    // Adding language field to match test expectations
    private String language;

    private String theme; // e.g., "dark", "light"

    // Setter to keep both fields in sync.
    public void setNotificationEnabled(boolean enabled) {
        this.notificationEnabled = enabled;
        this.notificationsEnabled = enabled; // Keep the persistent field in sync.
    }

    // Setter to keep both userId and auth0UserId in sync.
    public void setUserId(String userId) {
        this.userId = userId;
        this.auth0UserId = userId;
    }
}
