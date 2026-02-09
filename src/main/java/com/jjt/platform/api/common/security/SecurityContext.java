package com.jjt.platform.api.common.security;

import com.jjt.platform.config.security.Role;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
@Builder
public final class SecurityContext {
    Role role;
    UUID sponsorId;
    UUID orgId;
    
    @Builder
    public SecurityContext(Role role, UUID sponsorId, UUID orgId) {
        this.role = role;
        this.sponsorId = sponsorId;
        this.orgId = orgId;
    }
    
    public Role role() {
        return role;
    }

    public Optional<UUID> sponsorId() {
        return Optional.ofNullable(sponsorId);
    }

    public Optional<UUID> orgId() {
        return Optional.ofNullable(orgId);
    }
}
