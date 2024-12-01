package com.example.user_profile_information_store.repository;

import com.example.user_profile_information_store.model.RoutePreference;
import com.example.user_profile_information_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoutePreferenceRepository extends JpaRepository<RoutePreference, Long> {
    Optional<RoutePreference> findByUser(User user);
}
