package com.gridinsight.backend.RGMM_3.config;

import com.gridinsight.backend.RGMM_3.security.RgmmJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RgmmJwtAuthenticationFilter rgmmJwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Asset endpoints: managers and admins
                        .requestMatchers("/api/assets/**").hasAnyRole("ASSET_MANAGER", "ADMIN")

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(rgmmJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Let exceptions bubble up to your GlobalExceptionHandler
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Delegate to GlobalExceptionHandler by rethrowing
                            throw accessDeniedException;
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Delegate to GlobalExceptionHandler by rethrowing
                            throw authException;
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
