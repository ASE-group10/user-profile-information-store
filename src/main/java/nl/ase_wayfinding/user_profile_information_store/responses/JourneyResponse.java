package nl.ase_wayfinding.user_profile_information_store.responses;

public class JourneyResponse {
    private Long routeId;
    private String message;
    private String status; // e.g. "started" or "completed"

    public JourneyResponse() {}

    public JourneyResponse(Long routeId, String message, String status) {
        this.routeId = routeId;
        this.message = message;
        this.status = status;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
