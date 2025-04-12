package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

public class RouteTest {

    @Test
    void testAccessors() {
        Route route = new Route();
        route.setRouteId(1L);
        route.setStatus("started");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        route.setCreatedAt(ts);
        User user = new User();
        user.setAuth0UserId("auth0|1");
        route.setUser(user);

        assertEquals(1L, route.getRouteId());
        assertEquals("started", route.getStatus());
        assertEquals(ts, route.getCreatedAt());
        assertEquals("auth0|1", route.getUser().getAuth0UserId());
    }
}
