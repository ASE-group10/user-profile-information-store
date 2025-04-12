package nl.ase_wayfinding.user_profile_information_store.model;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;

public class JourneyLogTest {

    @Test
    void testAccessors() {
        JourneyLog log = new JourneyLog();
        log.setLogId(1L);
        log.setType("stop");
        log.setWaypoint("dummy");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        log.setTimestamp(ts);
        log.setStopName("Stop 1");

        assertEquals(1L, log.getLogId());
        assertEquals("stop", log.getType());
        assertEquals("dummy", log.getWaypoint());
        assertEquals(ts, log.getTimestamp());
        assertEquals("Stop 1", log.getStopName());
    }
}
