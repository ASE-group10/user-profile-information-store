package nl.ase_wayfinding.user_profile_information_store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ase_wayfinding.user_profile_information_store.model.Route;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.repository.RouteRepository;
import nl.ase_wayfinding.user_profile_information_store.resource.IncidentRequest;
import nl.ase_wayfinding.user_profile_information_store.responses.UserRoutesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTests {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RouteService routeService;

    private IncidentRequest incidentRequest;
    private List<Route> mockRoutes;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode mockJsonNode = realObjectMapper.readTree("{}");

        incidentRequest = new IncidentRequest();
        incidentRequest.setLatitude(52.3676);
        incidentRequest.setLongitude(4.9041);
        incidentRequest.setRadius(5.0);

        mockRoutes = new ArrayList<>();

        lenient().when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
    }

    @Test
    void testFindRoutesNearIncident_NoRoutesFound() {
        when(routeRepository.findRoutesNearLocation(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        UserRoutesResponse response = routeService.findRoutesNearIncident(incidentRequest);

        verify(routeRepository).findRoutesNearLocation(
                incidentRequest.getLatitude(),
                incidentRequest.getLongitude(),
                incidentRequest.getRadius()
        );
        assertNotNull(response);
        assertTrue(response.getRoutes().isEmpty());
    }

    @Test
    void testFindRoutesNearIncident_RoutesFoundButAllEnded() {
        Route endedRoute = createRoute("user1", "ended");
        mockRoutes.add(endedRoute);

        when(routeRepository.findRoutesNearLocation(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockRoutes);

        UserRoutesResponse response = routeService.findRoutesNearIncident(incidentRequest);

        verify(routeRepository).findRoutesNearLocation(
                incidentRequest.getLatitude(),
                incidentRequest.getLongitude(),
                incidentRequest.getRadius()
        );

        assertNotNull(response);
        assertTrue(response.getRoutes().isEmpty());
    }

    @Test
    void testFindRoutesNearIncident_ActiveRoutesFound() throws Exception {
        Route activeRoute1 = createRoute("user1", "active");
        Route activeRoute2 = createRoute("user2", "paused");
        Route endedRoute = createRoute("user3", "ended");

        mockRoutes.add(activeRoute1);
        mockRoutes.add(activeRoute2);
        mockRoutes.add(endedRoute);

        when(routeRepository.findRoutesNearLocation(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockRoutes);

        UserRoutesResponse response = routeService.findRoutesNearIncident(incidentRequest);

        verify(routeRepository).findRoutesNearLocation(
                incidentRequest.getLatitude(),
                incidentRequest.getLongitude(),
                incidentRequest.getRadius()
        );

        assertNotNull(response);
        assertEquals(2, response.getRoutes().size());

        verify(objectMapper, times(6)).readTree(anyString());
    }

    @Test
    void testFindRoutesNearIncident_JsonParsingException() throws Exception {
        Route activeRoute = createRoute("user1", "active");
        mockRoutes.add(activeRoute);

        when(routeRepository.findRoutesNearLocation(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockRoutes);

        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("JSON parsing error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            routeService.findRoutesNearIncident(incidentRequest);
        });

        assertEquals("Failed to parse route details", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("JSON parsing error"));
    }

    @Test
    void testFindRoutesNearIncident_CaseInsensitiveStatusCheck() {
        Route route1 = createRoute("user1", "ACTIVE");  // uppercase
        Route route2 = createRoute("user2", "Ended");   // mixed case
        Route route3 = createRoute("user3", "paused");  // lowercase

        mockRoutes.add(route1);
        mockRoutes.add(route2);
        mockRoutes.add(route3);

        when(routeRepository.findRoutesNearLocation(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockRoutes);

        UserRoutesResponse response = routeService.findRoutesNearIncident(incidentRequest);

        assertNotNull(response);
        assertEquals(2, response.getRoutes().size());
    }

    private Route createRoute(String auth0UserId, String status) {
        User user = new User();
        user.setAuth0UserId(auth0UserId);

        Route route = new Route();
        route.setUser(user);
        route.setStatus(status);
        route.setSourceLocation("{\"lat\": 52.3676, \"lng\": 4.9041}");
        route.setDestinationLocation("{\"lat\": 52.3680, \"lng\": 4.9045}");
        route.setWaypoints("[{\"lat\": 52.3678, \"lng\": 4.9043}]");

        return route;
    }
}