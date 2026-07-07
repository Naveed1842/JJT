package com.jjt.platform.api.media.dto;

import java.util.UUID;

public record MediaAttachmentResponse(
        UUID id,
        UUID mediaId,
        String attachmentRole,
        int sortOrder,
        String url,
        String thumbnailUrl,
        String mimeType,
        String mediaType,
        Integer widthPx,
        Integer heightPx,
        String status
) {}
