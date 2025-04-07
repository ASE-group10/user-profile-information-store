package nl.ase_wayfinding.user_profile_information_store.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "journey_logs")
public class JourneyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private RouteHistory routeHistory;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(columnDefinition = "text")
    private String waypoint;

    @Column
    private Timestamp timestamp;

    @Column(name = "stop_name")
    private String stopName;

    public JourneyLog() {}

    // Getters and Setters

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public RouteHistory getRoute() {
        return routeHistory;
    }

    public void setRoute(RouteHistory routeHistory) {
        this.routeHistory = routeHistory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(String waypoint) {
        this.waypoint = waypoint;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }
}
