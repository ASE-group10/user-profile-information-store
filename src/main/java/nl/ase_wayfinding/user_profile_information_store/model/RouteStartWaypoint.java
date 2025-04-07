package nl.ase_wayfinding.user_profile_information_store.model;

import jakarta.persistence.*;

@Entity
@Table(name = "route_start_waypoints")
public class RouteStartWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each start waypoint belongs to a Route.
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    // Optional: an order field in case you want to preserve the order of the waypoints.
    @Column(name = "waypoint_order")
    private Integer order;

    public RouteStartWaypoint() {}

    public RouteStartWaypoint(Route route, double latitude, double longitude, Integer order) {
        this.route = route;
        this.latitude = latitude;
        this.longitude = longitude;
        this.order = order;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
