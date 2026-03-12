package com.gridinsight.backend.z_common.security;

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
@EnableMethodSecurity // Enables @PreAuthorize on controllers/services
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Stateless REST API with JWT
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Return 401 for unauthenticated, 403 for forbidden
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, resp, e) -> resp.setStatus(401))
                        .accessDeniedHandler((req, resp, e) -> resp.setStatus(403))
                )

                .authorizeHttpRequests(auth -> auth
                        // ----- CORS preflight -----
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ----- PUBLIC auth -----
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/password/otp",
                                "/auth/password/reset"
                        ).permitAll()

                        // (If you still expose /auth/register, lock it to ADMIN; otherwise remove this)
                        .requestMatchers(HttpMethod.POST, "/auth/register").hasRole("ADMIN")

                        // ----- Swagger / OpenAPI (optional) -----
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ----- Admin area (e.g., /admin/users/**) -----
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ----- Load module (LMDAM_4) -----
                        // Read-only: GET for any authenticated user
                        .requestMatchers(HttpMethod.GET, "/load/**").authenticated()
                        // Mutations: POST/PUT/DELETE require ADMIN
                        .requestMatchers("/load/**").hasRole("ADMIN")

                        // ----- Audit -----
                        // Method-level guards exist, but we enforce again at route level
                        .requestMatchers("/audit/**").hasRole("ADMIN")

                        // ----- Any other authenticated APIs you may add -----
                        .requestMatchers("/users/**").authenticated()

                        // ----- Everything else requires authentication -----
                        .anyRequest().authenticated()
                )

                // Run JWT authentication before username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // No HTTP Basic for APIs
                .httpBasic(httpBasic -> httpBasic.disable());

        // If you need CORS for a local UI, uncomment the next line and the cors() bean below:
        // http.cors(cors -> {});

        return http.build();
    }


}
