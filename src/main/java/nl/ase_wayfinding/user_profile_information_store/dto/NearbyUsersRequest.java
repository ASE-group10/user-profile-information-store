package nl.ase_wayfinding.user_profile_information_store.dto;

public class NearbyUsersRequest {
    private double latitude;
    private double longitude;
    // Radius in meters
    private double radius;

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
    public double getRadius() {
        return radius;
    }
    public void setRadius(double radius) {
        this.radius = radius;
    }
}
