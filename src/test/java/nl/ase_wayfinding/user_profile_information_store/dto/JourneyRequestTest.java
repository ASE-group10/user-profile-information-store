package nl.ase_wayfinding.user_profile_information_store.dto;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

public class JourneyRequestTest {

    @Test
    void testJourneyRequestAccessors() {
        JourneyRequest req = new JourneyRequest();
        req.setRouteId(1L);
        req.setStopsFinished(2);
        req.setTotalStops(5);
        req.setTotalDistance(100.0);
        req.setTravelledDistance(60.0);
        req.setTotalWaypoints(10);
        req.setTravelledWaypoints(6);
        List<String> modes = Arrays.asList("car", "bike");
        req.setModesOfTransport(modes);

        JourneyRequest.JourneyHistory history = new JourneyRequest.JourneyHistory();
        JourneyRequest.Waypoint waypoint = new JourneyRequest.Waypoint();
        waypoint.setType("stop");
        waypoint.setStopName("Start");
        Map<String, Double> coords = new HashMap<>();
        coords.put("latitude", 40.0);
        coords.put("longitude", -74.0);
        waypoint.setWaypoint(coords);
        waypoint.setTimestamp(1234567890L);
        history.setWaypoints(Collections.singletonList(waypoint));
        req.setJourneyHistory(history);

        assertEquals(1L, req.getRouteId());
        assertEquals(2, req.getStopsFinished());
        assertEquals(5, req.getTotalStops());
        assertEquals(100.0, req.getTotalDistance());
        assertEquals(60.0, req.getTravelledDistance());
        assertEquals(10, req.getTotalWaypoints());
        assertEquals(6, req.getTravelledWaypoints());
        assertEquals(modes, req.getModesOfTransport());
        assertNotNull(req.getJourneyHistory());
        assertEquals("stop", req.getJourneyHistory().getWaypoints().get(0).getType());
    }
}
