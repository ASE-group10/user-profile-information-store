package nl.ase_wayfinding.user_profile_information_store.resource;

import com.fasterxml.jackson.databind.JsonNode;

public class RouteDetails {
    private JsonNode source;
    private JsonNode destination;
    private JsonNode routeDetails;
    private String status; // New field for route status

    // Getters and setters
    public JsonNode getSource() {
        return source;
    }

    public void setSource(JsonNode source) {
        this.source = source;
    }

    public JsonNode getDestination() {
        return destination;
    }

    public void setDestination(JsonNode destination) {
        this.destination = destination;
    }

    public JsonNode getRouteDetails() {
        return routeDetails;
    }

    public void setRouteDetails(JsonNode routeDetails) {
        this.routeDetails = routeDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
