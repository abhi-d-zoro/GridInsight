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
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
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

                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ ✅ PUBLIC AUTH ENDPOINTS (CRITICAL FIX)
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/password/otp",
                                "/api/v1/auth/password/reset"
                        ).permitAll()

                        // Register allowed only for ADMIN (if enabled)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register")
                        .hasRole("ADMIN")

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Load module (example)
                        .requestMatchers(HttpMethod.GET, "/load/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/load/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/load/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/load/**").hasRole("ADMIN")

                        // Audit
                        .requestMatchers("/audit/**").hasRole("ADMIN")

                        // Any other API requires authentication
                        .anyRequest().authenticated()
                )

                // ---- JWT filter ----
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ---- Disable defaults we don’t use ----
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    /*
     * Optional CORS configuration for local frontend
     * Enable only if calling from React/Angular directly
     *
     * @Bean
     * CorsConfigurationSource corsConfigurationSource() {
     *     var cfg = new CorsConfiguration();
     *     cfg.setAllowCredentials(true);
     *     cfg.setAllowedOrigins(List.of("http://localhost:5173"));
     *     cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
     *     cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));
     *     var source = new UrlBasedCorsConfigurationSource();
     *     source.registerCorsConfiguration("/**", cfg);
     *     return source;
     * }
     */
}
