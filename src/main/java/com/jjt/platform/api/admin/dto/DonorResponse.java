package com.jjt.platform.api.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record DonorResponse(
        UUID id,
        String displayName,
        String email,
        String phone,
        String donorType,
        String notes,
        Instant createdAt
) {}
