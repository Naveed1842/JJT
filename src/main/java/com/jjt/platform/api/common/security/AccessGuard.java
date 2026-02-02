package com.jjt.platform.api.common.security;

import com.jjt.platform.config.security.Role;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AccessGuard {

    private AccessGuard() {}

    public static SecurityContext requireRole(Role... allowed) {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx == null) {
            throw new UnauthorizedException("No security context");
        }
        Set<Role> set = Arrays.stream(allowed).collect(Collectors.toSet());
        if (!set.contains(ctx.role())) {
            throw new ForbiddenException("Role not permitted");
        }
        return ctx;
    }

    public static UUID requireSponsorId(SecurityContext ctx) {
        return ctx.sponsorId().orElseThrow(() -> new ForbiddenException("Sponsor id required"));
    }
}
