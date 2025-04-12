package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

public class RouteHistoryTest {

    @Test
    void testAccessors() {
        RouteHistory history = new RouteHistory();
        history.setRouteHistoryId(1L);
        Route route = new Route();
        route.setStatus("started");
        history.setRoute(route);
        history.setSourceLocation("StartLoc");
        history.setDestinationLocation("EndLoc");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        history.setCreatedAt(ts);

        assertEquals(1L, history.getRouteHistoryId());
        assertEquals("started", history.getRoute().getStatus());
        assertEquals("StartLoc", history.getSourceLocation());
        assertEquals("EndLoc", history.getDestinationLocation());
        assertEquals(ts, history.getCreatedAt());
    }
}
