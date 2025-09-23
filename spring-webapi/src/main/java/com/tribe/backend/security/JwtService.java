package com.tribe.backend.security;

import com.tribe.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenTtlSeconds());
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userPrincipal.getId().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim("email", userPrincipal.getUsername())
            .claim("roles", userPrincipal.getAuthorities().stream().map(Object::toString).toList())
            .signWith(signingKey)
            .compact();
    }

    public String generateAccessToken(UUID userId, String email, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenTtlSeconds());
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claims(claims)
            .claim("email", email)
            .signWith(signingKey)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Instant getExpiry(String token) {
        return parse(token).getExpiration().toInstant();
    }
}
