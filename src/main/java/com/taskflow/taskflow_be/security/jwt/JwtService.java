package com.taskflow.taskflow_be.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtService {

    private final JwtProperties props;
    private SecretKey signingKey;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

//    @PostConstruct
//    void init() {
//        String secret = props.getSecret();
//        if (secret == null || secret.isBlank()) {
//            throw new IllegalStateException("taskflow.jwt.secret must not be blank");
//        }
//
//        byte[] keyBytes;
//        try {
//            keyBytes = Decoders.BASE64.decode(secret);
//        } catch (Exception e) {
//            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
//        }
//
//        if (keyBytes.length < 32) {
//            throw new IllegalStateException("taskflow.jwt.secret must be at least 256 bits");
//        }
//
//        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
//    }

    public String generateAccessToken(String username, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getAccessTokenMinutes() * 60);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
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

    @PostConstruct
    void init() {
        String secret = props.getSecret();
        System.out.println("JWT secret raw = [" + secret + "]");

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("taskflow.jwt.secret must not be blank");
        }

        byte[] keyBytes;
        try {
            keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
            System.out.println("JWT secret decoded as BASE64, bytes = " + keyBytes.length);
        } catch (Exception e) {
            keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("JWT secret used as RAW string, bytes = " + keyBytes.length);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "taskflow.jwt.secret must be at least 32 bytes (256 bits). Current bytes = " + keyBytes.length
            );
        }

        this.signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }
}