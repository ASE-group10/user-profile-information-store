package nl.ase_wayfinding.user_profile_information_store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ase_wayfinding.user_profile_information_store.dto.JourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.NearbyUsersRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.StartJourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.responses.JourneyResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.NearbyUsersResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.RouteHistorySummaryResponse;
import nl.ase_wayfinding.user_profile_information_store.service.RouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testStartJourney() throws Exception {
        StartJourneyRequest startRequest = new StartJourneyRequest();
        // For this test, we simply use an empty list of waypoints.
        startRequest.setWaypoints(Collections.emptyList());

        // Create a dummy JourneyResponse.
        JourneyResponse journeyResponse = new JourneyResponse();
        // Assume JourneyResponse now has a 'status' property with getters and setters.
        journeyResponse.setStatus("started");

        // Stub the service call
        when(routeService.startJourney(any(StartJourneyRequest.class), any(HttpServletRequest.class)))
                .thenReturn(journeyResponse);

        // Convert the expected response to JSON string.
        String expectedJson = objectMapper.writeValueAsString(journeyResponse);

        mockMvc.perform(post("/api/routes/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void testCompleteJourney() throws Exception {
        JourneyRequest journeyRequest = new JourneyRequest();
        // Configure journeyRequest as needed

        // Create a dummy JourneyResponse.
        JourneyResponse journeyResponse = new JourneyResponse();
        journeyResponse.setStatus("completed");

        // Stub the service call.
        when(routeService.completeJourney(any(JourneyRequest.class), any(HttpServletRequest.class)))
                .thenReturn(journeyResponse);

        String expectedJson = objectMapper.writeValueAsString(journeyResponse);

        mockMvc.perform(post("/api/routes/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journeyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void testGetNearbyUsers() throws Exception {
        NearbyUsersRequest nearbyUsersRequest = new NearbyUsersRequest();
        nearbyUsersRequest.setLatitude(40.7128);
        nearbyUsersRequest.setLongitude(-74.0060);
        nearbyUsersRequest.setRadius(1000);

        List<String> phoneNumbers = List.of("+1234567890", "+0987654321");
        NearbyUsersResponse nearbyUsersResponse = new NearbyUsersResponse(phoneNumbers);

        when(routeService.findNearbyUsers(any(NearbyUsersRequest.class)))
                .thenReturn(nearbyUsersResponse);

        mockMvc.perform(post("/api/routes/nearby-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nearbyUsersRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(nearbyUsersResponse)));
    }


    @Test
    void testGetRouteHistory() throws Exception {
        List<RouteHistorySummaryResponse> historyResponses = Collections.emptyList();

        when(routeService.getRouteHistorySummaries(any(HttpServletRequest.class)))
                .thenReturn(historyResponses);

        String expectedJson = objectMapper.writeValueAsString(historyResponses);

        mockMvc.perform(get("/api/routes/history"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }
}
