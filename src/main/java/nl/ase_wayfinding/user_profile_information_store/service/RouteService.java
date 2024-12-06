package nl.ase_wayfinding.user_profile_information_store.service;

import nl.ase_wayfinding.user_profile_information_store.model.Route;
import nl.ase_wayfinding.user_profile_information_store.repository.RouteRepository;
import nl.ase_wayfinding.user_profile_information_store.resource.IncidentRequest;
import nl.ase_wayfinding.user_profile_information_store.resource.UserRouteInfo;
import nl.ase_wayfinding.user_profile_information_store.resource.RouteDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ase_wayfinding.user_profile_information_store.responses.UserRoutesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public UserRoutesResponse findRoutesNearIncident(IncidentRequest incidentRequest) {
        double latitude = incidentRequest.getLatitude();
        double longitude = incidentRequest.getLongitude();
        double radius = incidentRequest.getRadius();

        List<Route> routes = routeRepository.findRoutesNearLocation(latitude, longitude, radius);
        List<UserRouteInfo> result = new ArrayList<>();

        for (Route route : routes) {
            // Exclude routes with status "ended"
            if ("ended".equalsIgnoreCase(route.getStatus())) {
                continue;
            }

            UserRouteInfo info = new UserRouteInfo();
            info.setAuth0UserId(route.getUser().getAuth0UserId());

            RouteDetails routeDetails = new RouteDetails();

            try {
                // Parse JSON strings into objects
                routeDetails.setSource(objectMapper.readTree(route.getSourceLocation()));
                routeDetails.setDestination(objectMapper.readTree(route.getDestinationLocation()));
                routeDetails.setRouteDetails(objectMapper.readTree(route.getWaypoints()));

                // Set the status from the Route entity
                routeDetails.setStatus(route.getStatus());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse route details", e);
            }

            info.setRoute(routeDetails);
            result.add(info);
        }

        UserRoutesResponse response = new UserRoutesResponse();
        response.setRoutes(result);

        return response;
    }
}
