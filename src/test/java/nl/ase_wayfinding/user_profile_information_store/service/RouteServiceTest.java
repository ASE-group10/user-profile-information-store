package nl.ase_wayfinding.user_profile_information_store.service;

import nl.ase_wayfinding.user_profile_information_store.dto.JourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.NearbyUsersRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.StartJourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.model.*;
import nl.ase_wayfinding.user_profile_information_store.repository.*;
import nl.ase_wayfinding.user_profile_information_store.responses.JourneyResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.NearbyUsersResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.RouteHistorySummaryResponse;
import nl.ase_wayfinding.user_profile_information_store.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RouteServiceTest {

    @InjectMocks
    private RouteService routeService;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @Mock
    private JourneyLogRepository journeyLogRepository;

    @Mock
    private RouteStartWaypointRepository routeStartWaypointRepository;

    @Mock
    private PreferencesRepository preferencesRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.web.client.RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private MockHttpServletRequest request;

    private final String auth0Domain = "https://test-auth0.com/";
    private final String rewardServiceUrl = "https://reward-service.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();

        // Inject values and mocks
        ReflectionTestUtils.setField(routeService, "auth0Domain", auth0Domain);
        ReflectionTestUtils.setField(routeService, "rewardServiceUrl", rewardServiceUrl);
        ReflectionTestUtils.setField(routeService, "restTemplate", restTemplate); // <--- Add this line
    }

    // ===== startJourney Tests =====

    @Test
    void testStartJourney_MissingAuthorizationHeader() {
        StartJourneyRequest startRequest = new StartJourneyRequest();
        startRequest.setWaypoints(Collections.emptyList());
        // Do not set Authorization header
        JourneyResponse response = routeService.startJourney(startRequest, request);
        assertNull(response.getRouteId());
        assertEquals("Missing Authorization header", response.getMessage());
        assertNull(response.getStatus());
    }

    @Test
    void testStartJourney_InvalidToken() {
        StartJourneyRequest startRequest = new StartJourneyRequest();
        startRequest.setWaypoints(Collections.emptyList());
        request.addHeader("Authorization", "Bearer invalidToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer invalidToken", auth0Domain))
                    .thenReturn(null);
            JourneyResponse response = routeService.startJourney(startRequest, request);
            assertNull(response.getRouteId());
            assertEquals("Invalid token", response.getMessage());
            assertNull(response.getStatus());
        }
    }

    @Test
    void testStartJourney_SuccessWithoutWaypoints() {
        StartJourneyRequest startRequest = new StartJourneyRequest();
        startRequest.setWaypoints(Collections.emptyList());
        request.addHeader("Authorization", "Bearer validToken");
        String auth0UserId = "auth0|user123";
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn(auth0UserId);

            User user = new User();
            user.setId(10L);
            user.setAuth0UserId(auth0UserId);
            when(userRepository.findByAuth0UserId(auth0UserId)).thenReturn(Optional.of(user));
            when(routeRepository.findByUserAndStatus(user, "started"))
                    .thenReturn(Collections.emptyList());
            // Simulate route saving
            Route route = new Route();
            route.setRouteId(100L);
            route.setStatus("started");
            route.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            route.setUser(user);
            when(routeRepository.save(any(Route.class))).thenReturn(route);

            JourneyResponse response = routeService.startJourney(startRequest, request);
            assertEquals(100L, response.getRouteId());
            assertEquals("Journey started successfully", response.getMessage());
            assertEquals("started", response.getStatus());
        }
    }

    @Test
    void testStartJourney_SuccessWithWaypoints() {
        StartJourneyRequest startRequest = new StartJourneyRequest();
        StartJourneyRequest.Waypoint waypoint = new StartJourneyRequest.Waypoint();
        waypoint.setLatitude(40.0);
        waypoint.setLongitude(-70.0);
        startRequest.setWaypoints(List.of(waypoint));
        request.addHeader("Authorization", "Bearer validToken");
        String auth0UserId = "auth0|user456";
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn(auth0UserId);

            User user = new User();
            user.setId(20L);
            user.setAuth0UserId(auth0UserId);
            when(userRepository.findByAuth0UserId(auth0UserId)).thenReturn(Optional.of(user));
            when(routeRepository.findByUserAndStatus(user, "started"))
                    .thenReturn(Collections.emptyList());
            Route route = new Route();
            route.setRouteId(200L);
            route.setStatus("started");
            route.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            route.setUser(user);
            when(routeRepository.save(any(Route.class))).thenReturn(route);
            // Simulate saving each waypoint
            when(routeStartWaypointRepository.save(any(RouteStartWaypoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            JourneyResponse response = routeService.startJourney(startRequest, request);
            assertEquals(200L, response.getRouteId());
            assertEquals("Journey started successfully", response.getMessage());
            assertEquals("started", response.getStatus());
            verify(routeStartWaypointRepository, times(1)).save(any(RouteStartWaypoint.class));
        }
    }

    // ===== completeJourney Tests =====

    @Test
    void testCompleteJourney_MissingAuthorizationHeader() {
        JourneyRequest journeyRequest = new JourneyRequest();
        JourneyResponse response = routeService.completeJourney(journeyRequest, request);
        assertNull(response.getRouteId());
        assertEquals("Missing Authorization header", response.getMessage());
    }

    @Test
    void testCompleteJourney_InvalidToken() {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setRouteId(10L);
        request.addHeader("Authorization", "Bearer invalidToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer invalidToken", auth0Domain))
                    .thenReturn(null);
            JourneyResponse response = routeService.completeJourney(journeyRequest, request);
            assertNull(response.getRouteId());
            assertEquals("Invalid token", response.getMessage());
        }
    }

    @Test
    void testCompleteJourney_MissingRouteId() {
        JourneyRequest journeyRequest = new JourneyRequest();
        // routeId is not set
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|user789");
            User user = new User();
            user.setId(30L);
            user.setAuth0UserId("auth0|user789");
            when(userRepository.findByAuth0UserId("auth0|user789")).thenReturn(Optional.of(user));
            JourneyResponse response = routeService.completeJourney(journeyRequest, request);
            assertNull(response.getRouteId());
            assertEquals("Route ID is missing in the request", response.getMessage());
        }
    }

    @Test
    void testCompleteJourney_RouteNotFound() {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setRouteId(999L);
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|user000");
            User user = new User();
            user.setId(40L);
            user.setAuth0UserId("auth0|user000");
            when(userRepository.findByAuth0UserId("auth0|user000")).thenReturn(Optional.of(user));
            when(routeRepository.findById(999L)).thenReturn(Optional.empty());
            Exception exception = assertThrows(RuntimeException.class,
                    () -> routeService.completeJourney(journeyRequest, request));
            assertTrue(exception.getMessage().contains("Route not found for id: 999"));
        }
    }

    @Test
    void testCompleteJourney_UnauthorizedRoute() {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setRouteId(50L);
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|user111");
            User user = new User();
            user.setId(100L);
            user.setAuth0UserId("auth0|user111");
            when(userRepository.findByAuth0UserId("auth0|user111")).thenReturn(Optional.of(user));
            // Create a route that belongs to a different user.
            User otherUser = new User();
            otherUser.setId(200L);
            otherUser.setAuth0UserId("auth0|user222");
            Route route = new Route();
            route.setRouteId(50L);
            route.setUser(otherUser);
            when(routeRepository.findById(50L)).thenReturn(Optional.of(route));
            JourneyResponse response = routeService.completeJourney(journeyRequest, request);
            assertNull(response.getRouteId());
            assertEquals("Unauthorized: Route does not belong to user", response.getMessage());
        }
    }

    @Test
    void testCompleteJourney_Success_AndRewardCall() throws Exception {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setRouteId(60L);

        // Build a simple journey history with two stop waypoints.
        JourneyRequest.JourneyHistory journeyHistory = new JourneyRequest.JourneyHistory();
        JourneyRequest.Waypoint wp1 = new JourneyRequest.Waypoint();
        wp1.setType("stop");
        wp1.setTimestamp(System.currentTimeMillis());
        Map<String, Double> wpMap1 = new HashMap<>();
        wpMap1.put("latitude", 10.0);
        wpMap1.put("longitude", 20.0);
        wp1.setWaypoint(wpMap1);
        wp1.setStopName("Start Stop");

        JourneyRequest.Waypoint wp2 = new JourneyRequest.Waypoint();
        wp2.setType("stop");
        wp2.setTimestamp(System.currentTimeMillis() + 600000); // 10 minutes later
        Map<String, Double> wpMap2 = new HashMap<>();
        wpMap2.put("latitude", 30.0);
        wpMap2.put("longitude", 40.0);
        wp2.setWaypoint(wpMap2);
        wp2.setStopName("End Stop");

        journeyHistory.setWaypoints(List.of(wp1, wp2));
        journeyRequest.setJourneyHistory(journeyHistory);
        journeyRequest.setModesOfTransport(List.of("car", "bike"));
        journeyRequest.setTotalDistance(5000.0);
        journeyRequest.setTravelledDistance(3000.0);
        journeyRequest.setTotalWaypoints(10);
        journeyRequest.setTravelledWaypoints(5);

        request.addHeader("Authorization", "Bearer validToken");

        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|user333");

            User user = new User();
            user.setId(70L);
            user.setAuth0UserId("auth0|user333");
            when(userRepository.findByAuth0UserId("auth0|user333")).thenReturn(Optional.of(user));

            // Create a route that belongs to the user.
            Route route = new Route();
            route.setRouteId(60L);
            route.setStatus("started");
            // Set route created 20 minutes ago.
            Timestamp routeCreatedAt = new Timestamp(System.currentTimeMillis() - 1200000);
            route.setCreatedAt(routeCreatedAt);
            route.setUser(user);
            when(routeRepository.findById(60L)).thenReturn(Optional.of(route));
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Simulate saving RouteHistory.
            RouteHistory routeHistory = new RouteHistory();
            routeHistory.setRouteHistoryId(10L);
            routeHistory.setRoute(route);
            routeHistory.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            RouteDetail detail = new RouteDetail();
            detail.setTravelledDistance(3000.0);
            detail.setModesOfTransport("car,bike");
            routeHistory.setRouteDetail(detail);
            when(routeHistoryRepository.saveAndFlush(any(RouteHistory.class))).thenReturn(routeHistory);
            when(routeHistoryRepository.findById(10L)).thenReturn(Optional.of(routeHistory));

            // Simulate saving journey logs.
            when(journeyLogRepository.save(any(JourneyLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // For JSON conversion, delegate to a real ObjectMapper.
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> realMapper.writeValueAsString(invocation.getArgument(0)));

            // To simulate the reward service call, force an exception inside its try-catch block.
            // Since the code creates a new RestTemplate locally, we cannot inject one easily.
            // However, the reward call is in its own try-catch so we will simply let it throw.
            // (The exception will be caught and printed; it does not affect the journey completion.)
            // One way is to spy on RouteService and override new RestTemplate() but we assume the catch block is executed.
            when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok("Reward calculated successfully"));

            JourneyResponse response = routeService.completeJourney(journeyRequest, request);
            assertNotNull(response.getRouteId());
            assertEquals("Journey completed successfully", response.getMessage());
            assertEquals("completed", response.getStatus());
            // Verify that journey logs were saved for each waypoint in the journey history.
            verify(journeyLogRepository, times(2)).save(any(JourneyLog.class));
        }
    }

    @Test
    void testCompleteJourney_JsonProcessingException() throws Exception {
        JourneyRequest journeyRequest = new JourneyRequest();
        journeyRequest.setRouteId(70L);

        // 🛠️ Add minimal valid JourneyHistory with one waypoint
        JourneyRequest.JourneyHistory journeyHistory = new JourneyRequest.JourneyHistory();
        JourneyRequest.Waypoint wp = new JourneyRequest.Waypoint();
        wp.setType("stop");
        wp.setWaypoint(Map.of("latitude", 1.0, "longitude", 2.0));
        wp.setStopName("Example Stop");
        wp.setTimestamp(System.currentTimeMillis());
        journeyHistory.setWaypoints(List.of(wp));
        journeyRequest.setJourneyHistory(journeyHistory);

        request.addHeader("Authorization", "Bearer validToken");

        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|user777");

            User user = new User();
            user.setId(80L);
            user.setAuth0UserId("auth0|user777");
            when(userRepository.findByAuth0UserId("auth0|user777")).thenReturn(Optional.of(user));

            Route route = new Route();
            route.setRouteId(70L);
            route.setStatus("started");
            route.setCreatedAt(new Timestamp(System.currentTimeMillis() - 600000));
            route.setUser(user);
            when(routeRepository.findById(70L)).thenReturn(Optional.of(route));
            when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

            when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("test") {});

            JourneyResponse response = routeService.completeJourney(journeyRequest, request);
            assertNull(response.getRouteId());
            assertEquals("Error processing journey data", response.getMessage());
        }
    }


    // ===== findNearbyUsers Test =====

    @Test
    void testFindNearbyUsers() {
        NearbyUsersRequest req = new NearbyUsersRequest();
        req.setLatitude(50.0);
        req.setLongitude(8.0);
        req.setRadius(5000.0); // 5 km

        // Create two RouteStartWaypoint instances.
        RouteStartWaypoint wp1 = new RouteStartWaypoint();
        Route route1 = new Route();
        User user1 = new User();
        user1.setPhoneNumber("+1111111");
        user1.setAuth0UserId("auth0|user111");
        route1.setUser(user1);
        wp1.setRoute(route1);

        RouteStartWaypoint wp2 = new RouteStartWaypoint();
        Route route2 = new Route();
        User user2 = new User();
        user2.setPhoneNumber("+2222222");
        user2.setAuth0UserId("auth0|user222");
        route2.setUser(user2);
        wp2.setRoute(route2);

        List<RouteStartWaypoint> waypoints = List.of(wp1, wp2);
        // Convert radius to km (5000 / 1000 = 5)
        when(routeStartWaypointRepository.findStartedWaypointsNear(50.0, 8.0, 5.0)).thenReturn(waypoints);

        // For user1, notifications enabled; for user2, notifications disabled.
        Preferences pref1 = new Preferences();
        pref1.setAuth0UserId("auth0|user111");
        pref1.setNotificationsEnabled(true);
        when(preferencesRepository.findByAuth0UserId("auth0|user111")).thenReturn(Optional.of(pref1));

        Preferences pref2 = new Preferences();
        pref2.setAuth0UserId("auth0|user222");
        pref2.setNotificationsEnabled(false);
        when(preferencesRepository.findByAuth0UserId("auth0|user222")).thenReturn(Optional.of(pref2));

        NearbyUsersResponse response = routeService.findNearbyUsers(req);
        // Expect only user1's phone number to be included.
        assertEquals(List.of("+1111111"), response.getPhoneNumbers());
    }

    // ===== getRouteHistorySummaries Tests =====

    @Test
    void testGetRouteHistorySummaries_MissingAuthHeader() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> routeService.getRouteHistorySummaries(request));
        assertEquals("Missing Authorization header", exception.getMessage());
    }

    @Test
    void testGetRouteHistorySummaries_InvalidToken() {
        request.addHeader("Authorization", "Bearer invalidToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer invalidToken", auth0Domain))
                    .thenReturn(null);
            Exception exception = assertThrows(RuntimeException.class,
                    () -> routeService.getRouteHistorySummaries(request));
            assertEquals("Invalid token", exception.getMessage());
        }
    }

    @Test
    void testGetRouteHistorySummaries_UserNotFound() {
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|nonexistent");
            when(userRepository.findByAuth0UserId("auth0|nonexistent")).thenReturn(Optional.empty());
            Exception exception = assertThrows(RuntimeException.class,
                    () -> routeService.getRouteHistorySummaries(request));
            assertTrue(exception.getMessage().contains("User not found for token: auth0|nonexistent"));
        }
    }

    @Test
    void testGetRouteHistorySummaries_Success() throws Exception {
        request.addHeader("Authorization", "Bearer validToken");

        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|user123");

            User user = new User();
            user.setId(500L);
            user.setAuth0UserId("auth0|user123");
            when(userRepository.findByAuth0UserId("auth0|user123")).thenReturn(Optional.of(user));

            // Create a RouteHistory record.
            RouteHistory history = new RouteHistory();
            history.setRouteHistoryId(1000L);
            history.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            Route route = new Route();
            route.setCreatedAt(new Timestamp(System.currentTimeMillis() - 600000)); // 10 min ago
            history.setRoute(route);

            RouteDetail detail = new RouteDetail();
            detail.setTravelledDistance(2500.0);
            detail.setModesOfTransport("bus,tram");
            history.setRouteDetail(detail);

            when(routeHistoryRepository.findByRoute_User_Id(500L)).thenReturn(List.of(history));

            // Create two journey logs with type "stop"
            JourneyLog log1 = new JourneyLog();
            log1.setType("stop");
            log1.setStopName("A");
            log1.setTimestamp(new Timestamp(System.currentTimeMillis() - 550000));

            JourneyLog log2 = new JourneyLog();
            log2.setType("stop");
            log2.setStopName("B");
            log2.setTimestamp(new Timestamp(System.currentTimeMillis() - 500000));

            when(journeyLogRepository.findByRouteHistory(history)).thenReturn(List.of(log1, log2));

            // Delegate object mapping calls to real ObjectMapper.
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> realMapper.writeValueAsString(invocation.getArgument(0)));

            // Act
            List<RouteHistorySummaryResponse> summaries = routeService.getRouteHistorySummaries(request);

            // Assert
            assertFalse(summaries.isEmpty());
            RouteHistorySummaryResponse summary = summaries.get(0);

            assertEquals(1000L, summary.getRouteHistoryId());
            assertEquals("A", summary.getStartStopName());
            assertEquals("B", summary.getEndStopName());
            assertEquals(2, summary.getStopCount());
            assertEquals(List.of("A", "B"), summary.getStops());
            assertNotNull(summary.getDateLabel());
            assertNotNull(summary.getTravelledTime());
            assertEquals(List.of("bus", "tram"), summary.getModesOfTransport());
        }
    }

    @Test
    void testStartJourney_WithExistingRoutes() {
        // Prepare a StartJourneyRequest with no waypoints (the focus is on handling existing routes).
        StartJourneyRequest startRequest = new StartJourneyRequest();
        startRequest.setWaypoints(Collections.emptyList());
        request.addHeader("Authorization", "Bearer validToken");

        String auth0UserId = "auth0|userExisting";
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn(auth0UserId);

            User user = new User();
            user.setId(1L);
            user.setAuth0UserId(auth0UserId);
            when(userRepository.findByAuth0UserId(auth0UserId)).thenReturn(Optional.of(user));

            // Simulate that the user already has a started route.
            Route startedRoute = new Route();
            startedRoute.setRouteId(50L);
            startedRoute.setStatus("started");
            startedRoute.setUser(user);
            when(routeRepository.findByUserAndStatus(user, "started"))
                    .thenReturn(List.of(startedRoute));

            // When saving the completed route, return the route with status updated
            when(routeRepository.save(any(Route.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Also simulate saving of the new route (the one we are about to create)
            Route newRoute = new Route();
            newRoute.setRouteId(60L);
            newRoute.setStatus("started");
            newRoute.setUser(user);
            when(routeRepository.save(argThat(route -> "started".equals(route.getStatus()))))
                    .thenReturn(newRoute);

            JourneyResponse response = routeService.startJourney(startRequest, request);

            // Verify that our existing started route was updated to "completed".
            assertEquals(60L, response.getRouteId());
            assertEquals("Journey started successfully", response.getMessage());
            assertEquals("started", response.getStatus());

            // Verify that routeRepository.save was called once for the existing route (now with status "completed")
            verify(routeRepository, atLeastOnce()).save(argThat(route -> "completed".equals(route.getStatus())));
        }
    }

    @Test
    void testGetRouteHistorySummaries_DateLabel_Today() throws Exception {
        // Arrange – set up a history with creation date as today.
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|todayUser");

            User user = new User();
            user.setId(101L);
            user.setAuth0UserId("auth0|todayUser");
            when(userRepository.findByAuth0UserId("auth0|todayUser")).thenReturn(Optional.of(user));

            // Create a history record dated today.
            RouteHistory history = new RouteHistory();
            history.setRouteHistoryId(2000L);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            history.setCreatedAt(now);

            // Set route created 15 minutes before now so that computeTravelledTime returns a positive value.
            Route route = new Route();
            route.setCreatedAt(new Timestamp(System.currentTimeMillis() - 15 * 60 * 1000));
            history.setRoute(route);

            RouteDetail detail = new RouteDetail();
            detail.setTravelledDistance(1000.0);
            detail.setModesOfTransport("bike,car");
            history.setRouteDetail(detail);

            when(routeHistoryRepository.findByRoute_User_Id(101L)).thenReturn(List.of(history));

            // Simulate journey logs (can be empty for this date label test)
            when(journeyLogRepository.findByRouteHistory(history)).thenReturn(Collections.emptyList());

            // Use a real ObjectMapper for conversion.
            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> realMapper.writeValueAsString(invocation.getArgument(0)));

            List<RouteHistorySummaryResponse> summaries = routeService.getRouteHistorySummaries(request);

            // Assert – check that the date label indicates "Today".
            assertFalse(summaries.isEmpty());
            String dateLabel = summaries.get(0).getDateLabel();
            assertTrue(dateLabel.startsWith("Today at "));
        }
    }

    @Test
    void testGetRouteHistorySummaries_DateLabel_Yesterday() throws Exception {
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|yesterdayUser");

            User user = new User();
            user.setId(102L);
            user.setAuth0UserId("auth0|yesterdayUser");
            when(userRepository.findByAuth0UserId("auth0|yesterdayUser")).thenReturn(Optional.of(user));

            // Create a history record with creation date as yesterday.
            RouteHistory history = new RouteHistory();
            history.setRouteHistoryId(2001L);
            // Create a timestamp for yesterday (e.g., 24 hours ago)
            Timestamp yesterday = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            history.setCreatedAt(yesterday);

            // Set the route creation time before history creation time.
            Route route = new Route();
            route.setCreatedAt(new Timestamp(System.currentTimeMillis() - 25 * 60 * 60 * 1000));
            history.setRoute(route);

            RouteDetail detail = new RouteDetail();
            detail.setTravelledDistance(1500.0);
            detail.setModesOfTransport("tram,metro");
            history.setRouteDetail(detail);

            when(routeHistoryRepository.findByRoute_User_Id(102L)).thenReturn(List.of(history));
            when(journeyLogRepository.findByRouteHistory(history)).thenReturn(Collections.emptyList());

            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> realMapper.writeValueAsString(invocation.getArgument(0)));

            List<RouteHistorySummaryResponse> summaries = routeService.getRouteHistorySummaries(request);
            assertFalse(summaries.isEmpty());
            String dateLabel = summaries.get(0).getDateLabel();
            assertTrue(dateLabel.startsWith("Yesterday at "));
        }
    }

    @Test
    void testGetRouteHistorySummaries_DateLabel_Other() throws Exception {
        request.addHeader("Authorization", "Bearer validToken");
        try (MockedStatic<TokenUtils> tokenUtilsMock = Mockito.mockStatic(TokenUtils.class)) {
            tokenUtilsMock.when(() -> TokenUtils.extractAuth0UserIdFromToken("Bearer validToken", auth0Domain))
                    .thenReturn("auth0|oldUser");

            User user = new User();
            user.setId(103L);
            user.setAuth0UserId("auth0|oldUser");
            when(userRepository.findByAuth0UserId("auth0|oldUser")).thenReturn(Optional.of(user));

            // Create a history record with a date older than yesterday.
            RouteHistory history = new RouteHistory();
            history.setRouteHistoryId(2002L);
            // Use a timestamp 5 days ago.
            Timestamp fiveDaysAgo = new Timestamp(System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000);
            history.setCreatedAt(fiveDaysAgo);

            // Create a route with an earlier created date.
            Route route = new Route();
            route.setCreatedAt(new Timestamp(System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000 - 600000));
            history.setRoute(route);

            RouteDetail detail = new RouteDetail();
            detail.setTravelledDistance(3000.0);
            detail.setModesOfTransport("bus,tram");
            history.setRouteDetail(detail);

            when(routeHistoryRepository.findByRoute_User_Id(103L)).thenReturn(List.of(history));
            when(journeyLogRepository.findByRouteHistory(history)).thenReturn(Collections.emptyList());

            ObjectMapper realMapper = new ObjectMapper();
            when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> realMapper.writeValueAsString(invocation.getArgument(0)));

            List<RouteHistorySummaryResponse> summaries = routeService.getRouteHistorySummaries(request);
            assertFalse(summaries.isEmpty());
            String dateLabel = summaries.get(0).getDateLabel();
            // Since the "Other" branch uses a formatter with the pattern "MMM dd, yyyy HH:mm"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            String expected = formatter.format(fiveDaysAgo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            assertEquals(expected, dateLabel);
        }
    }
}