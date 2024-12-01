package com.example.user_profile_information_store.controller;

import com.example.user_profile_information_store.service.RouteService;
import com.example.user_profile_information_store.resource.IncidentRequest;
import com.example.user_profile_information_store.resource.UserRouteInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/near-incident")
    public ResponseEntity<?> getRoutesNearIncident(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody IncidentRequest incidentRequest) {

        String auth0UserId = jwt.getSubject();

        if (!auth0UserId.equals(incidentRequest.getAuth0UserId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid auth0_user_id.");
        }

        List<UserRouteInfo> usersWithRoutes = routeService.findRoutesNearIncident(incidentRequest);
        return ResponseEntity.ok(usersWithRoutes);
    }
}
