package com.example.user_profile_information_store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;

@Entity
@Table(name = "route_preferences")
@Schema(description = "User route preference details")
public class RoutePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the route preference", example = "1")
    private Long preferenceId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Prevents infinite recursion during serialization
    @Schema(hidden = true) // Hides the user object in API documentation
    private User user;

    @Column(name = "avoid_highways", nullable = false)
    @Schema(description = "Indicates if the user wants to avoid highways", example = "false")
    private Boolean avoidHighways = false;

    @Column(name = "avoid_tolls", nullable = false)
    @Schema(description = "Indicates if the user wants to avoid toll roads", example = "true")
    private Boolean avoidTolls = false;

    @Column(name = "preferred_mode", nullable = false)
    @Schema(description = "User's preferred mode of transportation", example = "bicycle")
    private String preferredMode;

    @Column(name = "eco_friendly", nullable = false)
    @Schema(description = "Indicates if the user prefers eco-friendly routes", example = "true")
    private Boolean ecoFriendly = true;

    @Column(name = "minimize_co2", nullable = false)
    @Schema(description = "Indicates if the user wants to minimize CO2 emissions", example = "false")
    private Boolean minimizeCo2 = false;

    @Column(name = "avoid_dangerous_streets", nullable = false)
    @Schema(description = "Indicates if the user wants to avoid dangerous streets", example = "true")
    private Boolean avoidDangerousStreets = true;

    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "Timestamp of the last update to preferences", example = "2024-12-01T12:34:56")
    private Timestamp lastUpdated;

    // Constructors
    public RoutePreference() {}

    // Getters and Setters
    public Long getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getAvoidHighways() {
        return avoidHighways;
    }

    public void setAvoidHighways(Boolean avoidHighways) {
        this.avoidHighways = avoidHighways;
    }

    public Boolean getAvoidTolls() {
        return avoidTolls;
    }

    public void setAvoidTolls(Boolean avoidTolls) {
        this.avoidTolls = avoidTolls;
    }

    public String getPreferredMode() {
        return preferredMode;
    }

    public void setPreferredMode(String preferredMode) {
        this.preferredMode = preferredMode;
    }

    public Boolean getEcoFriendly() {
        return ecoFriendly;
    }

    public void setEcoFriendly(Boolean ecoFriendly) {
        this.ecoFriendly = ecoFriendly;
    }

    public Boolean getMinimizeCo2() {
        return minimizeCo2;
    }

    public void setMinimizeCo2(Boolean minimizeCo2) {
        this.minimizeCo2 = minimizeCo2;
    }

    public Boolean getAvoidDangerousStreets() {
        return avoidDangerousStreets;
    }

    public void setAvoidDangerousStreets(Boolean avoidDangerousStreets) {
        this.avoidDangerousStreets = avoidDangerousStreets;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
