package nl.ase_wayfinding.user_profile_information_store.repository;

import nl.ase_wayfinding.user_profile_information_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByAuth0UserId(String auth0UserId);
}
