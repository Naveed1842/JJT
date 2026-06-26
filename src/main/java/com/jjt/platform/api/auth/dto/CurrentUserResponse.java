package com.jjt.platform.api.auth.dto;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String role,
        UUID sponsorId,
        UUID orgId) {}
