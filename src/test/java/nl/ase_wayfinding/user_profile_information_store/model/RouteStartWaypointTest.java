package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RouteStartWaypointTest {

    @Test
    void testAccessors() {
        RouteStartWaypoint wp = new RouteStartWaypoint();
        wp.setLatitude(40.0);
        wp.setLongitude(-74.0);
        wp.setOrder(1);
        Route route = new Route();
        route.setStatus("started");
        wp.setRoute(route);

        assertEquals(40.0, wp.getLatitude());
        assertEquals(-74.0, wp.getLongitude());
        assertEquals(1, wp.getOrder());
        assertEquals("started", wp.getRoute().getStatus());
    }
}
