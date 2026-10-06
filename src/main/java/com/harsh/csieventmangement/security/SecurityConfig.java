package com.harsh.csieventmangement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for the CSI Events backend.
 *
 * <p><strong>Breaking change fixed — Spring Boot 3.4.x:</strong>
 * In Spring Boot 3.2.x, DaoAuthenticationProvider had a constructor:
 * {@code new DaoAuthenticationProvider(userDetailsService)}
 *
 * In Spring Boot 3.4.x that constructor was REMOVED. The constructor
 * now only accepts a {@link PasswordEncoder}:
 * {@code new DaoAuthenticationProvider(passwordEncoder)}
 *
 * The UserDetailsService must now be set separately via:
 * {@code provider.setUserDetailsService(userDetailsService)}
 *
 * Reference — Spring Security 6.4 migration guide:
 * https://docs.spring.io/spring-security/reference/migration/index.html
 *
 * File: src/main/java/com/harsh/csieventmangement/security/SecurityConfig.java
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter     jwtAuthenticationFilter;
    private final CustomUserDetailsService    userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    // =========================================================================
    // Security Filter Chain
    // =========================================================================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF — not needed for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // Enable CORS using the bean from CorsConfig
                .cors(cors -> {})

                // Stateless — no HTTP sessions, every request carries a JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Return JSON 401/403 instead of HTML error pages
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler((request, response, denied) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"You don't have permission to do that.\",\"data\":null}");
                        })
                )

                // Access rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()  // login + register
                        .requestMatchers("/", "/health", "/error").permitAll()  // health check + error page
                        // Public pages required by Google Play (privacy policy, account deletion)
                        .requestMatchers(HttpMethod.GET,
                                "/privacy-policy", "/privacy-policy.html",
                                "/delete-account", "/delete-account.html").permitAll()
                        .anyRequest().authenticated()              // everything else needs JWT
                )

                // Register the custom authentication provider
                .authenticationProvider(authenticationProvider())

                // JWT filter runs before Spring's default username/password filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =========================================================================
    // Authentication Provider
    // =========================================================================

    /**
     * Wires together UserDetailsService and BCrypt password encoder.
     *
     * FIX: Spring Boot 3.4.x removed the DaoAuthenticationProvider(UserDetailsService)
     * constructor. Must now use no-arg constructor + setUserDetailsService() separately.
     *
     * OLD (Spring Boot 3.2.x — broken on 3.4.x):
     *   new DaoAuthenticationProvider(userDetailsService)
     *
     * NEW (Spring Boot 3.4.x — correct):
     *   new DaoAuthenticationProvider()
     *   provider.setUserDetailsService(userDetailsService)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // FIX: set UserDetailsService via setter (constructor no longer accepts it)
        provider.setUserDetailsService(userDetailsService);

        // BCrypt for password hashing and verification
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // =========================================================================
    // Password Encoder
    // =========================================================================

    /**
     * BCrypt password encoder with default strength (10 rounds).
     * Used to hash passwords on registration and verify them on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}