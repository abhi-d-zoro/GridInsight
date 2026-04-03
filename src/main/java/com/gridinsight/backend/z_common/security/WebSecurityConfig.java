package com.gridinsight.backend.z_common.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                // ✅ ✅ ✅ CORRECT CORS WIRING (SPRING SECURITY 6.x)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ---- Stateless REST API ----
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ---- Global exception handling ----
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, resp, e) -> resp.setStatus(401))
                        .accessDeniedHandler((req, resp, e) -> resp.setStatus(403))
                )

                // ---- Authorization rules ----
                .authorizeHttpRequests(auth -> auth

                        // ✅ Allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Public auth endpoints
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/password/otp",
                                "/api/v1/auth/password/reset"
                        ).permitAll()

                        // Register allowed only for ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register")
                        .hasRole("ADMIN")

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Load module
                        .requestMatchers(HttpMethod.GET, "/load/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/load/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/load/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/load/**").hasRole("ADMIN")

                        // Audit
                        .requestMatchers("/audit/**").hasRole("ADMIN")

                        // Everything else
                        .anyRequest().authenticated()
                )

                // ---- JWT filter ----
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ---- Disable unused defaults ----
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    // ✅ ✅ ✅ GLOBAL CORS CONFIGURATION
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.setAllowedOrigins(List.of("http://localhost:5173"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}