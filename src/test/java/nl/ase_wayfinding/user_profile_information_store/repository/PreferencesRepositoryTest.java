package nl.ase_wayfinding.user_profile_information_store.repository;

import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PreferencesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PreferencesRepository preferencesRepository;

    @Test
    void testFindByAuth0UserId() {
        // Create and persist test preferences
        Preferences preferences = new Preferences();
        preferences.setAuth0UserId("auth0|123456");
        preferences.setNotificationsEnabled(true);
        preferences.setLanguage("en");
        preferences.setTheme("light");
        entityManager.persist(preferences);
        entityManager.flush();

        // Test findByAuth0UserId
        Optional<Preferences> foundOptional = preferencesRepository.findByAuth0UserId("auth0|123456");

        // Verify
        assertTrue(foundOptional.isPresent());
        Preferences found = foundOptional.get();
        assertEquals("auth0|123456", found.getAuth0UserId());
        assertTrue(found.isNotificationsEnabled());
        assertEquals("en", found.getLanguage());
        assertEquals("light", found.getTheme());
    }

    @Test
    void testFindByAuth0UserId_NotFound() {
        // Test findByAuth0UserId with non-existent id
        Optional<Preferences> foundOptional = preferencesRepository.findByAuth0UserId("auth0|nonexistent");

        // Verify
        assertFalse(foundOptional.isPresent());
    }

    // Removing findByUserId tests as this method does not exist in the repository
}
