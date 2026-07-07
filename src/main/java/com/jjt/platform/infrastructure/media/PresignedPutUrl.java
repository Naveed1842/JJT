package com.jjt.platform.infrastructure.media;

import java.time.Instant;

public record PresignedPutUrl(
        String uploadUrl,
        String storageRef,
        Instant expiresAt
) {}
