package com.gridinsight.backend.a_iam.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String CLAIM_ROLES = "roles";

    private final Key key;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        byte[] keyBytes = decodeSecret(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (io.jsonwebtoken.io.DecodingException ex) {
            byte[] raw = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (raw.length < 32) {
                throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
            }
            return raw;
        }
    }

    /**
     * New: generate token with roles claim.
     */
    public String generateAccessToken(Long userId, Set<String> roles, long expiresInSeconds) {
        Instant now = Instant.now();
        // normalize roles to uppercase, stable order
        Set<String> norm = roles == null ? Set.of()
                : roles.stream().map(r -> r.toUpperCase().trim()).collect(Collectors.toCollection(LinkedHashSet::new));

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(CLAIM_ROLES, norm)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Backward-compatible (without roles). Prefer the overload above.
     */
    @Deprecated
    public String generateAccessToken(Long userId, long expiresInSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extract roles from token. Returns empty set if claim is absent.
     */
    public Set<String> parseRoles(String token) {
        Claims claims = parseClaims(token);
        Object rolesObj = claims.get(CLAIM_ROLES);
        if (rolesObj == null) return Set.of();

        if (rolesObj instanceof Collection<?> col) {
            return col.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.toUpperCase().trim())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        // Single string fallback
        return Set.of(rolesObj.toString().toUpperCase().trim());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}