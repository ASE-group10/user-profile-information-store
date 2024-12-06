package nl.ase_wayfinding.user_profile_information_store.responses;

import nl.ase_wayfinding.user_profile_information_store.resource.UserRouteInfo;

import java.util.List;

public class UserRoutesResponse {
    private List<UserRouteInfo> routes;

    public List<UserRouteInfo> getRoutes() {
        return routes;
    }

    public void setRoutes(List<UserRouteInfo> routes) {
        this.routes = routes;
    }
}
