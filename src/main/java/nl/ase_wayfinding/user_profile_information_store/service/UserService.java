package nl.ase_wayfinding.user_profile_information_store.service;

import nl.ase_wayfinding.user_profile_information_store.model.RoutePreference;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.repository.RoutePreferenceRepository;
import nl.ase_wayfinding.user_profile_information_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoutePreferenceRepository preferenceRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.orElse(null);
    }

    public RoutePreference getUserPreferences(String auth0UserId) {
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isPresent()) {
            Optional<RoutePreference> prefOpt = preferenceRepository.findByUser(userOpt.get());
            return prefOpt.orElse(null);
        }
        return null;
    }
}
