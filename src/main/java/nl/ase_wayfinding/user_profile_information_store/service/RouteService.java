package nl.ase_wayfinding.user_profile_information_store.service;

import jakarta.servlet.http.HttpServletRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.NearbyUsersRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.StartJourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.model.*;
import nl.ase_wayfinding.user_profile_information_store.repository.*;
import nl.ase_wayfinding.user_profile_information_store.dto.JourneyRequest;
import nl.ase_wayfinding.user_profile_information_store.responses.JourneyResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.NearbyUsersResponse;
import nl.ase_wayfinding.user_profile_information_store.responses.RouteHistorySummaryResponse;
import nl.ase_wayfinding.user_profile_information_store.util.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteHistoryRepository routeHistoryRepository;

    @Autowired
    private JourneyLogRepository journeyLogRepository;

    @Autowired
    private RouteStartWaypointRepository routeStartWaypointRepository;

    @Autowired
    private PreferencesRepository preferencesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${service.internal-urls.reward}")
    private String rewardServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    public JourneyResponse startJourney(StartJourneyRequest startJourneyRequest, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return new JourneyResponse(null, "Missing Authorization header", null);
        }

        String auth0UserId = TokenUtils.extractAuth0UserIdFromToken(authHeader, auth0Domain);
        if (auth0UserId == null) {
            return new JourneyResponse(null, "Invalid token", null);
        }

        User user = userRepository.findByAuth0UserId(auth0UserId)
                .orElseThrow(() -> new RuntimeException("User not found for token: " + auth0UserId));

        // Check for an existing "started" route and mark it as "completed"
        List<Route> existingRoutes = routeRepository.findByUserAndStatus(user, "started");
        if (!existingRoutes.isEmpty()) {
            for (Route existingRoute : existingRoutes) {
                existingRoute.setStatus("completed");
                routeRepository.save(existingRoute);
            }
        }

        // Create a new Route with a default status of "started" and current timestamp
        Route route = new Route();
        route.setStatus("started");
        route.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        route.setUser(user);

        Route savedRoute = routeRepository.save(route);

        // If waypoints are provided, save each as a RouteStartWaypoint
        List<StartJourneyRequest.Waypoint> waypoints = startJourneyRequest.getWaypoints();
        if (waypoints != null && !waypoints.isEmpty()) {
            int order = 0;
            for (StartJourneyRequest.Waypoint wp : waypoints) {
                RouteStartWaypoint rsw = new RouteStartWaypoint();
                rsw.setRoute(savedRoute);
                rsw.setLatitude(wp.getLatitude());
                rsw.setLongitude(wp.getLongitude());
                rsw.setOrder(order++);
                routeStartWaypointRepository.save(rsw);
            }
        }

        return new JourneyResponse(savedRoute.getRouteId(), "Journey started successfully", savedRoute.getStatus());
    }

    public JourneyResponse completeJourney(JourneyRequest journeyRequest, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || authHeader.isEmpty()) {
                return new JourneyResponse(null, "Missing Authorization header", null);
            }

            // Extract Auth0 user ID from the token.
            String auth0UserId = TokenUtils.extractAuth0UserIdFromToken(authHeader, auth0Domain);
            if (auth0UserId == null) {
                return new JourneyResponse(null, "Invalid token", null);
            }

            // Find the user.
            User user = userRepository.findByAuth0UserId(auth0UserId)
                    .orElseThrow(() -> new RuntimeException("User not found for token: " + auth0UserId));

            // Retrieve the route using the routeId provided.
            Long routeId = journeyRequest.getRouteId();
            if (routeId == null) {
                return new JourneyResponse(null, "Route ID is missing in the request", null);
            }
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new RuntimeException("Route not found for id: " + routeId));

            // Verify that the route belongs to the authenticated user.
            if (!route.getUser().getId().equals(user.getId())) {
                return new JourneyResponse(null, "Unauthorized: Route does not belong to user", null);
            }

            // Mark the route as "completed" and save.
            route.setStatus("completed");
            Route updatedRoute = routeRepository.save(route);

            // Create and populate a RouteHistory record.
            RouteHistory routeHistory = new RouteHistory();
            routeHistory.setRoute(updatedRoute);
            routeHistory.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // Process journey history waypoints for source and destination.
            List<JourneyRequest.Waypoint> waypoints = journeyRequest.getJourneyHistory().getWaypoints();
            if (waypoints != null && !waypoints.isEmpty()) {
                JourneyRequest.Waypoint sourceWp = null;
                JourneyRequest.Waypoint destWp = null;
                for (JourneyRequest.Waypoint wp : waypoints) {
                    if ("stop".equalsIgnoreCase(wp.getType())) {
                        if (sourceWp == null) {
                            sourceWp = wp;
                        }
                        destWp = wp;
                    }
                }
                if (sourceWp != null) {
                    String sourceJson = objectMapper.writeValueAsString(sourceWp.getWaypoint());
                    routeHistory.setSourceLocation(sourceJson);
                }
                if (destWp != null) {
                    String destJson = objectMapper.writeValueAsString(destWp.getWaypoint());
                    routeHistory.setDestinationLocation(destJson);
                }
            }

            // Create and attach RouteDetail with journey details.
            RouteDetail details = new RouteDetail();
            String modesJson = objectMapper.writeValueAsString(journeyRequest.getModesOfTransport());
            details.setModesOfTransport(modesJson);
            details.setTotalDistance(journeyRequest.getTotalDistance());
            details.setTravelledDistance(journeyRequest.getTravelledDistance());
            details.setTotalWaypoints(journeyRequest.getTotalWaypoints());
            details.setTravelledWaypoints(journeyRequest.getTravelledWaypoints());
            routeHistory.setRouteDetail(details);

            // Persist (and flush) the RouteHistory so that its ID is generated.
            routeHistory = routeHistoryRepository.saveAndFlush(routeHistory);
            // Re-fetch the persistent RouteHistory to ensure it is managed.
            Long routeHistoryId = routeHistory.getRouteHistoryId();
            RouteHistory managedRouteHistory = routeHistoryRepository.findById(routeHistoryId)
                    .orElseThrow(() -> new RuntimeException("RouteHistory not found after flush"));

            // Call the reward service.
            double travelledDistanceKm = journeyRequest.getTravelledDistance() / 1000.0;
            String rewardApiUrl = rewardServiceUrl + "/calculate-reward";

            Map<String, Object> routeDetails = new HashMap<>();
            routeDetails.put("distance", travelledDistanceKm);
            routeDetails.put("healthCompliant", true);

            Map<String, Object> rewardRequest = new HashMap<>();
            rewardRequest.put("routeDetails", routeDetails);

            // Prepare headers: content type and forward the Authorization header.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authHeader);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(rewardRequest, headers);

            try {
                ResponseEntity<String> rewardResponse = restTemplate.postForEntity(rewardApiUrl, entity, String.class);
                if (rewardResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Reward calculated successfully: " + rewardResponse.getBody());
                } else {
                    System.err.println("Failed to calculate reward: " + rewardResponse.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Error while calling /calculate-reward: " + e.getMessage());
            }

            // Save journey logs; use the managed RouteHistory instance for the association.
            for (JourneyRequest.Waypoint wp : journeyRequest.getJourneyHistory().getWaypoints()) {
                JourneyLog log = new JourneyLog();
                // Set the association using the managed RouteHistory.
                log.setRoute(managedRouteHistory);
                log.setType(wp.getType());
                String waypointJson = objectMapper.writeValueAsString(wp.getWaypoint());
                log.setWaypoint(waypointJson);
                log.setTimestamp(new Timestamp(wp.getTimestamp()));
                log.setStopName(wp.getStopName());
                journeyLogRepository.save(log);
            }

            // Return a successful response.
            return new JourneyResponse(updatedRoute.getRouteId(), "Journey completed successfully", updatedRoute.getStatus());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new JourneyResponse(null, "Error processing journey data", null);
        }
    }


    public NearbyUsersResponse findNearbyUsers(NearbyUsersRequest request) {
        // Convert radius from meters to kilometers
        double radiusKm = request.getRadius() / 1000.0;
        List<RouteStartWaypoint> waypoints = routeStartWaypointRepository.findStartedWaypointsNear(
                request.getLatitude(), request.getLongitude(), radiusKm);

        // Use a Set to avoid duplicate phone numbers
        Set<String> phoneNumbersSet = new HashSet<>();

        for (RouteStartWaypoint wp : waypoints) {
            if (wp.getRoute() != null) {
                User user = wp.getRoute().getUser();
                if (user != null && user.getPhoneNumber() != null) {
                    // Retrieve the user's preferences based on their Auth0 user ID
                    Optional<Preferences> optionalPreferences = preferencesRepository.findByAuth0UserId(user.getAuth0UserId());
                    // Only add the phone number if preferences exist and notifications are enabled
                    if (optionalPreferences.isPresent() && optionalPreferences.get().isNotificationsEnabled()) {
                        phoneNumbersSet.add(user.getPhoneNumber());
                    }
                }
            }
        }

        List<String> phoneNumbers = new ArrayList<>(phoneNumbersSet);
        return new NearbyUsersResponse(phoneNumbers);
    }

    public List<RouteHistorySummaryResponse> getRouteHistorySummaries(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            throw new RuntimeException("Missing Authorization header");
        }
        String auth0UserId = TokenUtils.extractAuth0UserIdFromToken(authHeader, auth0Domain);
        if (auth0UserId == null) {
            throw new RuntimeException("Invalid token");
        }
        User user = userRepository.findByAuth0UserId(auth0UserId)
                .orElseThrow(() -> new RuntimeException("User not found for token: " + auth0UserId));

        // Retrieve all RouteHistory records for this user
        List<RouteHistory> histories = routeHistoryRepository.findByRoute_User_Id(user.getId());
        List<RouteHistorySummaryResponse> summaries = new ArrayList<>();

        for (RouteHistory history : histories) {
            // Retrieve all journey logs for this history
            List<JourneyLog> logs = journeyLogRepository.findByRouteHistory(history);
            // Filter logs to only those with type "stop" and sort by timestamp
            List<JourneyLog> stopLogs = logs.stream()
                    .filter(log -> "stop".equalsIgnoreCase(log.getType()))
                    .sorted(Comparator.comparing(JourneyLog::getTimestamp))
                    .collect(Collectors.toList());

            String startStopName = "";
            String endStopName = "";
            int stopCount = 0;
            List<String> stops = new ArrayList<>();

            if (!stopLogs.isEmpty()) {
                startStopName = stopLogs.get(0).getStopName();
                endStopName = stopLogs.get(stopLogs.size() - 1).getStopName();
                stopCount = stopLogs.size();
                stops = stopLogs.stream()
                        .map(JourneyLog::getStopName)
                        .collect(Collectors.toList());
            }

            double travelledDistance = (history.getRouteDetail() != null)
                    ? history.getRouteDetail().getTravelledDistance() : 0.0;

            // Example: Compute travelled time.
            // In a real implementation, you might compute (lastTimestamp - firstTimestamp) / (1000*60)
            long travelledTimeMinutes = computeTravelledTime(history);
            String travelledTime = formatDuration(travelledTimeMinutes);

            String dateLabel = computeDateLabel(history.getCreatedAt());

            // Aggregate modes of transport. Assuming they are stored as a comma-separated string.
            List<String> modes = (history.getRouteDetail() != null && history.getRouteDetail().getModesOfTransport() != null)
                    ? List.of(history.getRouteDetail().getModesOfTransport().split(","))
                    : new ArrayList<>();

            RouteHistorySummaryResponse summary = new RouteHistorySummaryResponse(
                    history.getRouteHistoryId(),
                    startStopName,
                    endStopName,
                    travelledDistance,
                    travelledTime,
                    dateLabel,
                    modes,
                    stopCount,
                    stops
            );

            summaries.add(summary);
        }

        return summaries;
    }

    // Helper method to extract stop name from the stored source/destination (adjust based on your format)
    private String extractStopName(String locationData) {
        // For example, if the locationData is plain text with a stop name, return it;
        // otherwise, you may need to parse a JSON object. Here we return as is.
        return locationData;
    }

    // Helper method to compute travelled time. Replace with your actual implementation.
    private long computeTravelledTime(RouteHistory history) {
        // Calculate time difference in minutes between when the route was started and when it was completed
        Date routeCreatedAt = history.getRoute().getCreatedAt();
        Date historyCreatedAt = history.getCreatedAt();
        if (routeCreatedAt != null && historyCreatedAt != null) {
            return (historyCreatedAt.getTime() - routeCreatedAt.getTime()) / (1000 * 60);
        }
        return 0;
    }


    // Helper method to format duration given minutes into "xh ym"
    private String formatDuration(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    private String computeDateLabel(Date date) {
        LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate historyDate = dateTime.toLocalDate();
        LocalDate today = LocalDate.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        if (historyDate.equals(today)) {
            return "Today at " + dateTime.format(timeFormatter);
        } else if (historyDate.equals(today.minusDays(1))) {
            return "Yesterday at " + dateTime.format(timeFormatter);
        } else {
            return dateTime.format(dateTimeFormatter);
        }
    }



}
