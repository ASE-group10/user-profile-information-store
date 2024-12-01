package com.example.user_profile_information_store.repository;

import com.example.user_profile_information_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByAuth0UserId(String auth0UserId);
}
