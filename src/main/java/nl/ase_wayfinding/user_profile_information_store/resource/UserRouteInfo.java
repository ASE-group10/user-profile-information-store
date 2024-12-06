package nl.ase_wayfinding.user_profile_information_store.resource;

public class UserRouteInfo {
    private String auth0UserId;
    private RouteDetails route;

    // Getters and setters
    public String getAuth0UserId() {
        return auth0UserId;
    }

    public void setAuth0UserId(String auth0UserId) {
        this.auth0UserId = auth0UserId;
    }

    public RouteDetails getRoute() {
        return route;
    }

    public void setRoute(RouteDetails route) {
        this.route = route;
    }
}
