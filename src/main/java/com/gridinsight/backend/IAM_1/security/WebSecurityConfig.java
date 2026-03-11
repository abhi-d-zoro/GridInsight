package com.gridinsight.backend.IAM_1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity// <-- Enables @PreAuthorize annotations
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Stateless API with JWT
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Allow ONLY these auth endpoints without a token
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/password/otp",
                                "/auth/password/reset"
                        ).permitAll()

                        // IMPORTANT: Remove public register. Two options:

                        // Option A (recommended): remove the /register endpoint entirely
                        // OR
                        // Option B: keep it but restrict to ADMIN only (if you kept the route)
                        .requestMatchers(HttpMethod.POST, "/auth/register").hasRole("ADMIN")

                        // (Optional) docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Admin-only management APIs (new convention)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Other protected APIs
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/audit/**").authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Your JWT filter should authenticate only when Authorization header is present
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Disable HTTP Basic for API
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}