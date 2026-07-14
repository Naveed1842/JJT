package com.jjt.platform.api.media.dto;

import java.time.Instant;
import java.util.UUID;

public record MediaUploadIntentResponse(
        UUID mediaId,
        String uploadUrl,
        String storageRef,
        Instant expiresAt,
        long maxBytes
) {}
