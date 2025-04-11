package nl.ase_wayfinding.user_profile_information_store.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class NearbyUsersRequestTest {

    @Test
    void testAccessors() {
        NearbyUsersRequest request = new NearbyUsersRequest();
        request.setLatitude(40.0);
        request.setLongitude(-74.0);
        request.setRadius(5000);

        assertEquals(40.0, request.getLatitude());
        assertEquals(-74.0, request.getLongitude());
        assertEquals(5000, request.getRadius());
    }
}
