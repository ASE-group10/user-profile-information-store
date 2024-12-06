package nl.ase_wayfinding.user_profile_information_store.controller;

import nl.ase_wayfinding.user_profile_information_store.responses.UserRoutesResponse;
import nl.ase_wayfinding.user_profile_information_store.service.RouteService;
import nl.ase_wayfinding.user_profile_information_store.resource.IncidentRequest;
import nl.ase_wayfinding.user_profile_information_store.resource.UserRouteInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Operation(
        summary = "Get Routes Near Incident",
        description = "Retrieves a list of user routes that are near a given incident location."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved routes near the incident",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "array", implementation = UserRouteInfo.class),
                examples = @ExampleObject(
                    name = "200 OK Example",
                    value = "[\n" +
                            "    {\n" +
                            "        \"route\": {\n" +
                            "            \"source\": {\n" +
                            "                \"name\": \"Dublin\",\n" +
                            "                \"latitude\": 53.3498,\n" +
                            "                \"longitude\": -6.2603\n" +
                            "            },\n" +
                            "            \"destination\": {\n" +
                            "                \"name\": \"Charleville\",\n" +
                            "                \"latitude\": 52.1609,\n" +
                            "                \"longitude\": -8.6536\n" +
                            "            },\n" +
                            "            \"routeDetails\": [\n" +
                            "                {\n" +
                            "                    \"name\": \"Cashel\",\n" +
                            "                    \"latitude\": 52.35,\n" +
                            "                    \"longitude\": -7.85\n" +
                            "                }\n" +
                            "            ],\n" +
                            "            \"status\": \"active\"\n" +
                            "        }\n" +
                            "    },\n" +
                            "    {\n" +
                            "        \"auth0UserId\": \"auth0|6750fe6b7016716a3ef646f5\",\n" +
                            "        \"route\": {\n" +
                            "            \"source\": {\n" +
                            "                \"name\": \"Dublin\",\n" +
                            "                \"latitude\": 53.3498,\n" +
                            "                \"longitude\": -6.2603\n" +
                            "            },\n" +
                            "            \"destination\": {\n" +
                            "                \"name\": \"Charleville\",\n" +
                            "                \"latitude\": 52.1609,\n" +
                            "                \"longitude\": -8.6536\n" +
                            "            },\n" +
                            "            \"routeDetails\": [\n" +
                            "                {\n" +
                            "                    \"name\": \"Cashel\",\n" +
                            "                    \"latitude\": 52.35,\n" +
                            "                    \"longitude\": -7.85\n" +
                            "                }\n" +
                            "            ],\n" +
                            "            \"status\": \"active\"\n" +
                            "        }\n" +
                            "    }\n" +
                            "]"
                )
            )
        )
    })
    @PostMapping("/near-incident")
    public ResponseEntity<UserRoutesResponse> getRoutesNearIncident(
            @RequestBody IncidentRequest incidentRequest) {

        UserRoutesResponse usersWithRoutes = routeService.findRoutesNearIncident(incidentRequest);
        return ResponseEntity.ok(usersWithRoutes);
    }
}
