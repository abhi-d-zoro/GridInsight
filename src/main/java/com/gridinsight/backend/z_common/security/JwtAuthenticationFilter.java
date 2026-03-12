package com.gridinsight.backend.z_common.security;

import com.gridinsight.backend.IAM_1.entity.User;
import com.gridinsight.backend.IAM_1.entity.UserStatus;
import com.gridinsight.backend.IAM_1.repository.UserRepository;
import com.gridinsight.backend.IAM_1.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 1) Extract subject (userId) and roles from token
            Long userId = Long.valueOf(jwtService.parseSubject(token));
            Set<String> tokenRoles = jwtService.parseRoles(token); // may be empty if old tokens

            // Avoid re-auth if already set
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2) Load user (to verify ACTIVE + optionally fetch roles if token missing roles)
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Enforce ACTIVE users only (revocation-by-status)
            if (user.getStatus() != UserStatus.ACTIVE) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3) Build authorities:
            //    - Prefer roles from token (no DB hit for roles on every request)
            //    - If token lacks roles (older tokens), fall back to DB roles
            Set<String> effectiveRoles =
                    (tokenRoles == null || tokenRoles.isEmpty())
                            ? user.getRoles().stream().map(r -> r.getName().toUpperCase().trim()).collect(Collectors.toSet())
                            : tokenRoles;

            List<SimpleGrantedAuthority> authorities = effectiveRoles.stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // Spring expects ROLE_ prefix for hasRole()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            // 4) Create Authentication and store in context
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ignored) {
            // Invalid/expired token? -> continue without authentication
        }

        filterChain.doFilter(request, response);
    }
}