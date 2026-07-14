package com.jjt.platform.api.media.dto;

public record MediaVariantResponse(
        String variantType,
        String url,
        String mimeType,
        long sizeBytes,
        Integer widthPx,
        Integer heightPx
) {}
