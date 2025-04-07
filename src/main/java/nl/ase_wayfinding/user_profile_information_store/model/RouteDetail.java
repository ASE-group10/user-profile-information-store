package nl.ase_wayfinding.user_profile_information_store.model;

import jakarta.persistence.*;

@Entity
@Table(name = "route_details")
public class RouteDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeDetailsId;

    @Column(name = "modes_of_transport", columnDefinition = "text")
    private String modesOfTransport;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "travelled_distance")
    private Double travelledDistance;

    @Column(name = "total_waypoints")
    private Integer totalWaypoints;

    @Column(name = "travelled_waypoints")
    private Integer travelledWaypoints;

    // Constructors
    public RouteDetail() {}

    public RouteDetail(String modesOfTransport, Double totalDistance, Double travelledDistance, Integer totalWaypoints, Integer travelledWaypoints) {
        this.modesOfTransport = modesOfTransport;
        this.totalDistance = totalDistance;
        this.travelledDistance = travelledDistance;
        this.totalWaypoints = totalWaypoints;
        this.travelledWaypoints = travelledWaypoints;
    }

    // Getters and Setters
    public Long getRouteDetailsId() {
        return routeDetailsId;
    }

    public void setRouteDetailsId(Long routeDetailsId) {
        this.routeDetailsId = routeDetailsId;
    }

    public String getModesOfTransport() {
        return modesOfTransport;
    }

    public void setModesOfTransport(String modesOfTransport) {
        this.modesOfTransport = modesOfTransport;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Double getTravelledDistance() {
        return travelledDistance;
    }

    public void setTravelledDistance(Double travelledDistance) {
        this.travelledDistance = travelledDistance;
    }

    public Integer getTotalWaypoints() {
        return totalWaypoints;
    }

    public void setTotalWaypoints(Integer totalWaypoints) {
        this.totalWaypoints = totalWaypoints;
    }

    public Integer getTravelledWaypoints() {
        return travelledWaypoints;
    }

    public void setTravelledWaypoints(Integer travelledWaypoints) {
        this.travelledWaypoints = travelledWaypoints;
    }
}
