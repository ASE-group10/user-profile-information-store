package nl.ase_wayfinding.user_profile_information_store.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "route_histories")
public class RouteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeHistoryId;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "source_location", columnDefinition = "text")
    private String sourceLocation;

    @Column(name = "destination_location", columnDefinition = "text")
    private String destinationLocation;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_details_id")
    private RouteDetail routeDetail;

    // Constructors
    public RouteHistory() {}

    public RouteHistory(Route route, String sourceLocation, String destinationLocation) {
        this.route = route;
        this.sourceLocation = sourceLocation;
        this.destinationLocation = destinationLocation;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public Long getRouteHistoryId() {
        return routeHistoryId;
    }

    public void setRouteHistoryId(Long routeHistoryId) {
        this.routeHistoryId = routeHistoryId;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public RouteDetail getRouteDetail() {
        return routeDetail;
    }

    public void setRouteDetail(RouteDetail routeDetail) {
        this.routeDetail = routeDetail;
    }
}
