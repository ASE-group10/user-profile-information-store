package nl.ase_wayfinding.user_profile_information_store.repository;

import nl.ase_wayfinding.user_profile_information_store.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        // Create and persist a test user
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setAuth0Id("auth0|123456");
        entityManager.persist(user);
        entityManager.flush();

        // Test findByEmail
        Optional<User> foundOptional = userRepository.findByEmail("test@example.com");

        // Verify
        assertTrue(foundOptional.isPresent());
        User found = foundOptional.get();
        assertEquals("test@example.com", found.getEmail());
        assertEquals("Test User", found.getName());
        assertEquals("auth0|123456", found.getAuth0Id());
    }

    @Test
    void testFindByEmail_NotFound() {
        // Test findByEmail with non-existent email
        Optional<User> foundOptional = userRepository.findByEmail("nonexistent@example.com");

        // Verify
        assertFalse(foundOptional.isPresent());
    }

    // Removing findByAuth0Id tests as this method does not exist in the repository
}
