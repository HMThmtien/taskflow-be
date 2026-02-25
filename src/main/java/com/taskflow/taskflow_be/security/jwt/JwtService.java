package com.taskflow.taskflow_be.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    void validateSecret() {
        if (props.getSecret() == null || props.getSecret().length() < 32) {
            throw new IllegalStateException("taskflow.jwt.secret must be at least 32 characters for HS256");
        }
    }

    public String generateAccessToken(String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getAccessTokenMinutes() * 60);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            Claims c = parseClaims(token);
            return c.getExpiration() != null && c.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? "USER" : String.valueOf(role);
    }
}
