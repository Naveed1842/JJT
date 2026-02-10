package com.jjt.platform.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String DEFAULT_SECRET = "jjt-platform-secret-key-for-jwt-token-generation-minimum-256-bits";
    private static final int MINIMUM_KEY_LENGTH = 32; // 256 bits

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long expiration;
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        // Use default only in dev/local profiles
        if (secret == null || secret.isEmpty()) {
            boolean isDevEnvironment = activeProfile != null && 
                (activeProfile.contains("dev") || activeProfile.contains("local"));
            
            if (isDevEnvironment) {
                logger.warn("JWT secret not configured, using default secret for development");
                secret = DEFAULT_SECRET;
            } else {
                throw new IllegalStateException(
                    "JWT secret must be explicitly configured via JWT_SECRET environment variable or jwt.secret property. " +
                    "Do not use default secrets in production!"
                );
            }
        }
        
        // Validate key length
        if (secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_KEY_LENGTH) {
            throw new IllegalStateException(
                "JWT secret must be at least " + MINIMUM_KEY_LENGTH + " bytes (256 bits) for HS256 algorithm"
            );
        }
        
        logger.info("JWT token provider initialized successfully");
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String role, UUID sponsorId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        if (sponsorId != null) {
            claims.put("sponsorId", sponsorId.toString());
        }
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public UUID extractSponsorId(String token) {
        String sponsorId = extractClaim(token, claims -> claims.get("sponsorId", String.class));
        return sponsorId != null ? UUID.fromString(sponsorId) : null;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // Check expiration from already-parsed claims
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
