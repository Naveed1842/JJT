package com.jjt.platform.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limits POST /api/auth/login to 10 attempts per minute per client IP.
 * Uses in-memory buckets — resets on restart, which is acceptable for stateless dynos.
 * Disabled when the "test" Spring profile is active so integration tests are unaffected.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final boolean enabled;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(Environment env) {
        this.enabled = !env.acceptsProfiles(Profiles.of("test"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        if (enabled && "POST".equals(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI())) {
            String ip = resolveClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);
            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                    "{\"code\":\"RATE_LIMITED\",\"message\":\"Too many login attempts. Try again in a minute.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS, Refill.greedy(MAX_REQUESTS, WINDOW));
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
