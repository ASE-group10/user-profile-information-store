package com.example.user_profile_information_store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SecurityConfig {

    @Value("${auth0.logout.return-url}")
    private String logoutReturnUrl;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/api/login", "/api/signup", "/api/forgot-password", "/api/logout"
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/api/login", 
                    "/api/signup", 
                    "/api/forgot-password", 
                    "/api/logout", 
                    "/error"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/auth0")
                .defaultSuccessUrl("/", true)
                .failureUrl("/error")
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    String auth0LogoutUrl = "https://test-auth0-domain.com/v2/logout" +
                        "?client_id=" + clientId +
                        "&returnTo=" + logoutReturnUrl;
                    response.sendRedirect(auth0LogoutUrl);
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );
        return http.build();
    }
}
