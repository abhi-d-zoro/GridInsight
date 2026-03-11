package com.gridinsight.backend.RGMM_3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

import com.gridinsight.backend.IAM_1.entity.User;
import com.gridinsight.backend.IAM_1.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component("rgmmJwtAuthenticationFilter") // <-- renamed bean
@RequiredArgsConstructor
public class RgmmJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        Long userId = jwtValidator.validateAndExtractUserId(token);
        if (userId != null) {
            User user = userRepo.findById(userId).orElse(null);
            if (user != null) {
                var authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .collect(Collectors.toSet());

                var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
