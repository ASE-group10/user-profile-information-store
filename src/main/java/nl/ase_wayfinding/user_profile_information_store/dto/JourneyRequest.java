package nl.ase_wayfinding.user_profile_information_store.dto;

import java.util.List;
import java.util.Map;

public class JourneyRequest {
    // New field for the route id (sent as a number in JSON)
    private Long routeId;

    private int stopsFinished;
    private int totalStops;
    private double totalDistance;
    private double travelledDistance;
    private int totalWaypoints;
    private int travelledWaypoints;
    private List<String> modesOfTransport;
    private JourneyHistory journeyHistory;

    // Getters and Setters

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public int getStopsFinished() {
        return stopsFinished;
    }

    public void setStopsFinished(int stopsFinished) {
        this.stopsFinished = stopsFinished;
    }

    public int getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(int totalStops) {
        this.totalStops = totalStops;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTravelledDistance() {
        return travelledDistance;
    }

    public void setTravelledDistance(double travelledDistance) {
        this.travelledDistance = travelledDistance;
    }

    public int getTotalWaypoints() {
        return totalWaypoints;
    }

    public void setTotalWaypoints(int totalWaypoints) {
        this.totalWaypoints = totalWaypoints;
    }

    public int getTravelledWaypoints() {
        return travelledWaypoints;
    }

    public void setTravelledWaypoints(int travelledWaypoints) {
        this.travelledWaypoints = travelledWaypoints;
    }

    public List<String> getModesOfTransport() {
        return modesOfTransport;
    }

    public void setModesOfTransport(List<String> modesOfTransport) {
        this.modesOfTransport = modesOfTransport;
    }

    public JourneyHistory getJourneyHistory() {
        return journeyHistory;
    }

    public void setJourneyHistory(JourneyHistory journeyHistory) {
        this.journeyHistory = journeyHistory;
    }

    public static class JourneyHistory {
        private List<Waypoint> waypoints;

        public List<Waypoint> getWaypoints() {
            return waypoints;
        }

        public void setWaypoints(List<Waypoint> waypoints) {
            this.waypoints = waypoints;
        }
    }

    public static class Waypoint {
        private String type;
        private String stopName;
        private Map<String, Double> waypoint; // contains "latitude" and "longitude"
        private long timestamp;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStopName() {
            return stopName;
        }

        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        public Map<String, Double> getWaypoint() {
            return waypoint;
        }

        public void setWaypoint(Map<String, Double> waypoint) {
            this.waypoint = waypoint;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
