package com.gridinsight.backend.IAM_1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Stateless API with JWT
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Allow auth endpoints without a token
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/password/forgot",
                                "/api/auth/password/reset"
                        ).permitAll()

                        // (Optional) docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // User management endpoints (will add RBAC in Phase 5)
                        .requestMatchers("/api/users/**").authenticated()

                        // Audit log endpoints
                        .requestMatchers("/api/audit/**").authenticated()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Your JWT filter should only authenticate when an Authorization header is present
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Disable HTTP Basic for API
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}