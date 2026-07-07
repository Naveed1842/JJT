package com.jjt.platform.api.media.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MediaFileResponse(
        UUID id,
        String mediaType,
        String mimeType,
        String originalName,
        long sizeBytes,
        Integer widthPx,
        Integer heightPx,
        String visibility,
        String status,
        String altText,
        Instant uploadedAt,
        List<MediaVariantResponse> variants
) {}
