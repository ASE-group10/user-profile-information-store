package nl.ase_wayfinding.user_profile_information_store.repository;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    Optional<Preferences> findByAuth0UserId(String auth0UserId);
}
