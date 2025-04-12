package nl.ase_wayfinding.user_profile_information_store.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testUserGettersAndSetters() {
        // Create a user object
        User user = new User();

        // Set values
        user.setId(1L);
        user.setAuth0UserId("auth0|123456");
        user.setEmail("user@example.com");
        user.setName("Test User");
        user.setPhoneNumber("+1234567890");
        user.setCreatedAt(null); // Let this be set by JPA

        // Test getters
        assertEquals(1L, user.getId());
        assertEquals("auth0|123456", user.getAuth0UserId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertEquals("+1234567890", user.getPhoneNumber());
    }

    @Test
    void testUserDefaultValues() {
        // Create a user with default values
        User user = new User();

        // Test default values
        assertNull(user.getId());
        assertNull(user.getAuth0UserId());
        assertNull(user.getEmail());
        assertNull(user.getName());
        assertNull(user.getPhoneNumber());
        assertNull(user.getCreatedAt());
    }
}
