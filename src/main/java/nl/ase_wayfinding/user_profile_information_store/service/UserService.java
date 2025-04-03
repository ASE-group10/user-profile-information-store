package nl.ase_wayfinding.user_profile_information_store.service;

import nl.ase_wayfinding.user_profile_information_store.dto.AccountUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.dto.PreferencesUpdateRequest;
import nl.ase_wayfinding.user_profile_information_store.model.Preferences;
import nl.ase_wayfinding.user_profile_information_store.model.User;
import nl.ase_wayfinding.user_profile_information_store.repository.PreferencesRepository;
import nl.ase_wayfinding.user_profile_information_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PreferencesRepository preferencesRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.orElse(null);
    }

    public Preferences getPreferences(String auth0UserId) {
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isPresent()) {
            Optional<Preferences> prefOpt = preferencesRepository.findByAuth0UserId(userOpt.get().getAuth0UserId());
            return prefOpt.orElse(null);
        }
        return null;
    }

    public void savePreferences(Preferences preferences) {
        preferencesRepository.save(preferences);
    }


    public void updatePreferences(String auth0UserId, PreferencesUpdateRequest request) {
        Preferences preferences = preferencesRepository.findByAuth0UserId(auth0UserId)
                .orElseGet(() -> {
                    Preferences newPref = new Preferences();
                    newPref.setAuth0UserId(auth0UserId);
                    return newPref;
                });

        preferences.setNotificationsEnabled(request.isNotificationsEnabled());
        preferences.setTheme(request.getTheme());
        preferencesRepository.save(preferences);
    }

    public void updateAccount(String auth0UserId, AccountUpdateRequest request) {
        User user = userRepository.findByAuth0UserId(auth0UserId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getPicture() != null) {
            user.setPicture(request.getPicture());
        }

        userRepository.save(user);
    }

}
