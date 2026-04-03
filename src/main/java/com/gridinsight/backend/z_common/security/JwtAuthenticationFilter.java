package com.gridinsight.backend.z_common.security;

import com.gridinsight.backend.a_iam.entity.User;
import com.gridinsight.backend.a_iam.entity.UserStatus;
import com.gridinsight.backend.a_iam.repository.UserRepository;
import com.gridinsight.backend.a_iam.security.JwtService;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // ✅ 1. Extract userId and role info from token
            Long userId = Long.valueOf(jwtService.parseSubject(token));
            Set<String> tokenRoles = jwtService.parseRoles(token);

            // ✅ Avoid re-authentication
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ 2. Load user from DB
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getStatus() != UserStatus.ACTIVE) {
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ 3. Resolve SINGLE role
            String roleName =
                    (tokenRoles != null && !tokenRoles.isEmpty())
                            ? tokenRoles.iterator().next()
                            : user.getRole().getName();

            String authority = roleName.startsWith("ROLE_")
                    ? roleName
                    : "ROLE_" + roleName;

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(authority));

            // ✅ 4. Build Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ignored) {
            // Invalid or expired token → continue without authentication
        }

        filterChain.doFilter(request, response);
    }
}