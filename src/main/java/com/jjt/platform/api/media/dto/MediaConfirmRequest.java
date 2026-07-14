package com.jjt.platform.api.media.dto;

public record MediaConfirmRequest(
        /** Optional SHA-256 hex hash for integrity verification */
        String contentHash
) {}
