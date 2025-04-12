package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RouteDetailTest {

    @Test
    void testAccessors() {
        RouteDetail detail = new RouteDetail();
        detail.setRouteDetailsId(1L);
        detail.setModesOfTransport("car,bike");
        detail.setTotalDistance(100.0);
        detail.setTravelledDistance(50.0);
        detail.setTotalWaypoints(10);
        detail.setTravelledWaypoints(5);

        assertEquals(1L, detail.getRouteDetailsId());
        assertEquals("car,bike", detail.getModesOfTransport());
        assertEquals(100.0, detail.getTotalDistance());
        assertEquals(50.0, detail.getTravelledDistance());
        assertEquals(10, detail.getTotalWaypoints());
        assertEquals(5, detail.getTravelledWaypoints());
    }
}
