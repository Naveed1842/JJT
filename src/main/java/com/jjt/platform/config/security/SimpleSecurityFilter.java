package com.jjt.platform.config.security;

import com.jjt.platform.api.common.security.ForbiddenException;
import com.jjt.platform.api.common.security.SecurityContext;
import com.jjt.platform.api.common.security.SecurityContextHolder;
import com.jjt.platform.api.common.security.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class SimpleSecurityFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final AuthProperties authProperties;

    public SimpleSecurityFilter(FirebaseTokenVerifier firebaseTokenVerifier, AuthProperties authProperties) {
        this.firebaseTokenVerifier = firebaseTokenVerifier;
        this.authProperties = authProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Allow CORS preflight requests to pass through without authentication
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            SecurityContext context = resolveContext(request);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    private SecurityContext resolveContext(HttpServletRequest request) {
        if (authProperties.getMode() == AuthMode.LEGACY) {
            return resolveLegacyContext(request);
        }

        String bearerToken = extractBearerToken(request);
        if (bearerToken != null) {
            var verified = firebaseTokenVerifier.verifyBearerToken(bearerToken)
                    .orElseThrow(() -> new UnauthorizedException("Missing or invalid Firebase token"));
            if (verified.role() == Role.SPONSOR && verified.sponsorId() == null) {
                throw new ForbiddenException("Sponsor role requires sponsor_id claim");
            }
            return new SecurityContext(verified.role(), verified.sponsorId(), verified.orgId());
        }

        if (authProperties.getMode() == AuthMode.FIREBASE) {
            throw new UnauthorizedException("Missing bearer token");
        }

        return resolveLegacyContext(request);
    }

    private SecurityContext resolveLegacyContext(HttpServletRequest request) {
        String roleHeader = request.getHeader("X-ROLE");
        Role role = Role.fromHeader(roleHeader);
        if (role == null) {
            throw new UnauthorizedException("Missing or invalid X-ROLE header");
        }
        UUID sponsorId = parseUuid(request.getHeader("X-SPONSOR-ID"));
        UUID orgId = parseUuid(request.getHeader("X-ORG-ID"));
        if (role == Role.SPONSOR && sponsorId == null) {
            throw new ForbiddenException("Sponsor role requires X-SPONSOR-ID header");
        }
        return new SecurityContext(role, sponsorId, orgId);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private UUID parseUuid(String header) {
        if (header == null || header.isBlank()) return null;
        try {
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/api/public/");
    }
}
