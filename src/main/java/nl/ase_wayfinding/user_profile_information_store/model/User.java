package nl.ase_wayfinding.user_profile_information_store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth0_user_id", unique = true, nullable = false)
    private String auth0UserId;

    // Adding auth0Id field to match test expectations
    private String auth0Id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    private String picture;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Add methods that tests are expecting
    public void setAuth0Id(String auth0Id) {
        this.auth0Id = auth0Id;
        this.auth0UserId = auth0Id; // Keep both fields in sync
    }
}
