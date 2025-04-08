package nl.ase_wayfinding.user_profile_information_store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class SecurityConfig {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${auth0.domain}")
    private String issuer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/api/login",
                "/api/signup",
                "/api/forgot-password",
                "/api/logout",
                "/api/users/**",
                "/api/routes/**"
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/api/login",
                    "/api/signup",
                    "/api/forgot-password",
                    "/api/logout",
                    "/api/routes/start",
                    "/api/routes/complete",
                    "/api/routes/nearby-users",
                    "/api/routes/history",
                    "/error",
                    "/swagger-ui/**", // Allow Swagger UI
                    "/swagger-ui/**",   // Allow Swagger UI
                    "/v3/api-docs/**",  // Allow API docs
                    "/v3/api-docs.yaml", // Allow YAML docs
                    "/swagger-ui.html", // Direct Swagger UI access
                    "/swagger-resources/**", // Required for Springfox-style Swagger
                    "/webjars/**" // Webjars for Swagger UI
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = issuer + ".well-known/jwks.json";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
