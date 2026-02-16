package com.jjt.platform.config.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class FirebaseTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenVerifier.class);
    private static final String APP_NAME = "jjt-platform-firebase";

    private final FirebaseAuthProperties properties;
    private volatile FirebaseAuth firebaseAuth;

    public FirebaseTokenVerifier(FirebaseAuthProperties properties) {
        this.properties = properties;
    }

    public Optional<VerifiedPrincipal> verifyBearerToken(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return Optional.empty();
        }
        FirebaseAuth auth = getFirebaseAuth();
        if (auth == null) {
            return Optional.empty();
        }
        try {
            FirebaseToken token = auth.verifyIdToken(bearerToken);
            Role role = parseRole(token.getClaims());
            if (role == null) {
                return Optional.empty();
            }
            UUID sponsorId = parseUuidClaim(token.getClaims(), "sponsor_id");
            UUID orgId = parseUuidClaim(token.getClaims(), "org_id");
            return Optional.of(new VerifiedPrincipal(role, sponsorId, orgId));
        } catch (Exception ex) {
            log.warn("Firebase token verification failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private FirebaseAuth getFirebaseAuth() {
        FirebaseAuth current = firebaseAuth;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (firebaseAuth != null) {
                return firebaseAuth;
            }
            String serviceAccountBase64 = properties.getServiceAccountBase64();
            if (serviceAccountBase64 == null || serviceAccountBase64.isBlank()) {
                log.info("Firebase auth is not configured (firebase.auth.service-account-base64 missing).");
                return null;
            }
            try {
                byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64.getBytes(StandardCharsets.UTF_8));
                GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
                FirebaseOptions.Builder builder = FirebaseOptions.builder().setCredentials(credentials);
                if (properties.getProjectId() != null && !properties.getProjectId().isBlank()) {
                    builder.setProjectId(properties.getProjectId());
                }

                FirebaseApp app = FirebaseApp.getApps().stream()
                        .filter(existing -> APP_NAME.equals(existing.getName()))
                        .findFirst()
                        .orElseGet(() -> FirebaseApp.initializeApp(builder.build(), APP_NAME));

                firebaseAuth = FirebaseAuth.getInstance(app);
                log.info("Firebase auth initialized successfully.");
                return firebaseAuth;
            } catch (Exception ex) {
                log.error("Failed to initialize Firebase auth: {}", ex.getMessage());
                return null;
            }
        }
    }

    private Role parseRole(Map<String, Object> claims) {
        Object role = claims.get("role");
        if (role instanceof String value) {
            return Role.fromHeader(value);
        }
        return null;
    }

    private UUID parseUuidClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(text.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public record VerifiedPrincipal(Role role, UUID sponsorId, UUID orgId) {}
}

