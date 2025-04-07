package nl.ase_wayfinding.user_profile_information_store.responses;

import java.util.List;

public class RouteHistorySummaryResponse {
    private Long routeHistoryId;
    private String startStopName;
    private String endStopName;
    private double travelledDistance; // in kilometers
    private String travelledTime;     // e.g., "1h 25m"
    private String dateLabel;         // "Today", "Yesterday", or a formatted date
    private List<String> modesOfTransport;
    private int stopCount;
    private List<String> stops;       // List of all stop names

    public RouteHistorySummaryResponse() {}

    public RouteHistorySummaryResponse(Long routeHistoryId, String startStopName, String endStopName,
                                       double travelledDistance, String travelledTime, String dateLabel,
                                       List<String> modesOfTransport, int stopCount, List<String> stops) {
        this.routeHistoryId = routeHistoryId;
        this.startStopName = startStopName;
        this.endStopName = endStopName;
        this.travelledDistance = travelledDistance;
        this.travelledTime = travelledTime;
        this.dateLabel = dateLabel;
        this.modesOfTransport = modesOfTransport;
        this.stopCount = stopCount;
        this.stops = stops;
    }

    public Long getRouteHistoryId() {
        return routeHistoryId;
    }

    public void setRouteHistoryId(Long routeHistoryId) {
        this.routeHistoryId = routeHistoryId;
    }

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }

    public String getEndStopName() {
        return endStopName;
    }

    public void setEndStopName(String endStopName) {
        this.endStopName = endStopName;
    }

    public double getTravelledDistance() {
        return travelledDistance;
    }

    public void setTravelledDistance(double travelledDistance) {
        this.travelledDistance = travelledDistance;
    }

    public String getTravelledTime() {
        return travelledTime;
    }

    public void setTravelledTime(String travelledTime) {
        this.travelledTime = travelledTime;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public List<String> getModesOfTransport() {
        return modesOfTransport;
    }

    public void setModesOfTransport(List<String> modesOfTransport) {
        this.modesOfTransport = modesOfTransport;
    }

    public int getStopCount() {
        return stopCount;
    }

    public void setStopCount(int stopCount) {
        this.stopCount = stopCount;
    }

    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        this.stops = stops;
    }
}
