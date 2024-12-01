package com.example.user_profile_information_store.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "source_location", columnDefinition = "jsonb")
    private String sourceLocation;

    @Column(name = "destination_location", columnDefinition = "jsonb")
    private String destinationLocation;

    @Column(columnDefinition = "jsonb")
    private String waypoints;

    private String status = "active";

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    // Constructors
    public Route() {}

    // Getters and Setters

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
       this.user = user;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
