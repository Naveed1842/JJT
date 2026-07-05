package com.jjt.platform.api.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String alertType,
        String severity,
        String title,
        String message,
        UUID relatedEntityId,
        String relatedEntityType,
        boolean dismissed,
        Instant dismissedAt,
        Instant createdAt
) {}
