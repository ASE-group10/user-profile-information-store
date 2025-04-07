package nl.ase_wayfinding.user_profile_information_store.controller;

import jakarta.servlet.http.HttpServletRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.JourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.NearbyUsersRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.StartJourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.responses.NearbyUsersResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.RouteHistorySummaryResponse;
import nl.ase_wayfinding.user_profile_information_store.service.RouteService;
import nl.ase_wayfinding.user_profile_information_store.responses.JourneyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/start")
    public JourneyResponse startJourney(@RequestBody StartJourneyRequest startJourneyRequest, HttpServletRequest request) {
        return routeService.startJourney(startJourneyRequest, request);
    }

    @PostMapping("/complete")
    public JourneyResponse completeJourney(@RequestBody JourneyRequest journeyRequest, HttpServletRequest request) {
        // Directly return the response from the service.
        return routeService.completeJourney(journeyRequest, request);
    }

    @PostMapping("/nearby-users")
    public NearbyUsersResponse getNearbyUsers(@RequestBody NearbyUsersRequest request, HttpServletRequest httpRequest) {
        return routeService.findNearbyUsers(request);
    }

    @GetMapping("/history")
    public List<RouteHistorySummaryResponse> getRouteHistory(HttpServletRequest request) {
        return routeService.getRouteHistorySummaries(request);
    }
}
