package com.gridinsight.backend.c_rgmm.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {

    private final String secret = "super-secret-key"; // externalize to config

    public Long validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
