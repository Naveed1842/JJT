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

// @Component - DISABLED: Now using JwtAuthenticationFilter instead
public class SimpleSecurityFilter extends OncePerRequestFilter {

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
