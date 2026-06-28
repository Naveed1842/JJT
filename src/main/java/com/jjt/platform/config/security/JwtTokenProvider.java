package com.jjt.platform.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SPONSOR_ID = "sponsorId";
    private static final String CLAIM_ORG_ID = "orgId";

    private final SecretKey signingKey;
    private final JwtProperties props;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.signingKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(JwtUserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.getAccessTokenExpiryMs());
        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getUsername())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(now)
                .expiration(expiry);
        if (user.getSponsorId() != null) {
            builder.claim(CLAIM_SPONSOR_ID, user.getSponsorId().toString());
        }
        if (user.getOrgId() != null) {
            builder.claim(CLAIM_ORG_ID, user.getOrgId().toString());
        }
        return builder.signWith(signingKey).compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public Role getRole(String token) {
        return Role.valueOf(parseClaims(token).get(CLAIM_ROLE, String.class));
    }

    public UUID getSponsorId(String token) {
        String val = parseClaims(token).get(CLAIM_SPONSOR_ID, String.class);
        return val != null ? UUID.fromString(val) : null;
    }

    public UUID getOrgId(String token) {
        String val = parseClaims(token).get(CLAIM_ORG_ID, String.class);
        return val != null ? UUID.fromString(val) : null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
