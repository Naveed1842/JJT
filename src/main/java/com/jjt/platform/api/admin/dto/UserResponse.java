package com.jjt.platform.api.admin.dto;

import com.jjt.platform.infrastructure.persistence.entity.UserEntity;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String role,
        UUID sponsorId,
        UUID orgId,
        boolean active,
        Instant createdAt,
        Instant lastLoginAt) {

    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getSponsorId(),
                user.getOrgId(),
                user.isActive(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
