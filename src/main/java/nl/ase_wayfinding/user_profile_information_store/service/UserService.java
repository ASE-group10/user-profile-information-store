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

    // Added to match test expectations
    public Optional<Preferences> getUserPreferences(String auth0UserId) {
        Preferences preferences = getPreferences(auth0UserId);
        return Optional.ofNullable(preferences);
    }

    public User getUserByAuth0Id(String auth0UserId) {
        return userRepository.findByAuth0UserId(auth0UserId).orElse(null);
    }

    // Added to match test expectations
    public Optional<User> getUserById(String auth0UserId) {
        User user = getUserByAuth0Id(auth0UserId);
        return Optional.ofNullable(user);
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

        // Handle both field names for backward compatibility
        if (request.isNotificationsEnabled() || request.isNotificationEnabled()) {
            preferences.setNotificationsEnabled(request.isNotificationsEnabled() || request.isNotificationEnabled());
            preferences.setNotificationEnabled(request.isNotificationsEnabled() || request.isNotificationEnabled());
        }

        preferences.setTheme(request.getTheme());

        // Handle the language field if present
        if (request.getLanguage() != null) {
            preferences.setLanguage(request.getLanguage());
        }

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

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);
    }
}
