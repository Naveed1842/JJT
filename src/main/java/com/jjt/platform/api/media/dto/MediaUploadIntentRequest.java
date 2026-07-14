package com.jjt.platform.api.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MediaUploadIntentRequest(
        @NotBlank String mimeType,
        @NotBlank String originalName,
        long sizeBytes,
        /** PUBLIC | PRIVATE | SIGNED */
        String visibility,
        @NotBlank String ownerType,
        @NotNull UUID ownerId,
        @NotBlank String attachmentRole,
        int sortOrder
) {}
