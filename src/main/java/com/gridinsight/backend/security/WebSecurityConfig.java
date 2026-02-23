package com.gridinsight.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // allow POST from Postman without CSRF token
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Open just the auth endpoints for now
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // For dev: either open everything or keep others authenticated.
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults()); // keep defaults light for dev

        return http.build();
    }
}