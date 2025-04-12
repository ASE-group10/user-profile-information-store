package nl.ase_wayfinding.user_profile_information_store.dto;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;

public class StartJourneyRequestTest {

    @Test
    void testWaypointsAccessors() {
        StartJourneyRequest request = new StartJourneyRequest();
        StartJourneyRequest.Waypoint wp1 = new StartJourneyRequest.Waypoint();
        wp1.setLatitude(40.0);
        wp1.setLongitude(-74.0);
        StartJourneyRequest.Waypoint wp2 = new StartJourneyRequest.Waypoint();
        wp2.setLatitude(41.0);
        wp2.setLongitude(-75.0);

        request.setWaypoints(Arrays.asList(wp1, wp2));

        assertNotNull(request.getWaypoints());
        assertEquals(2, request.getWaypoints().size());
        assertEquals(40.0, request.getWaypoints().get(0).getLatitude());
        assertEquals(-75.0, request.getWaypoints().get(1).getLongitude());
    }
}
