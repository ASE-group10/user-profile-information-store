package nl.ase_wayfinding.user_profile_information_store.model;


import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "preferences")
public class Preferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long auth0UserId;

    private boolean notificationsEnabled;

    private String theme; // e.g., "dark", "light"

}
