package com.example.productservice.JWTconfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public java.util.Set<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        String rolesCsv = claims.get("roles", String.class);
        if (rolesCsv == null || rolesCsv.isBlank()) return java.util.Collections.emptySet();
        String[] parts = rolesCsv.split(",");
        return java.util.Arrays.stream(parts).map(String::trim).collect(java.util.stream.Collectors.toSet());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public java.util.Set<String> extractAuthorities(String token) {
        Claims claims = getClaims(token);
        String permissions = claims.get("permissions", String.class);
        if (permissions == null || permissions.isBlank()) return java.util.Collections.emptySet();
        String[] parts = permissions.split(",");
        return java.util.Arrays.stream(parts).map(String::trim).collect(java.util.stream.Collectors.toSet());
    }
}
