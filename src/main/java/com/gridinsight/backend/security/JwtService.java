package com.gridinsight.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
