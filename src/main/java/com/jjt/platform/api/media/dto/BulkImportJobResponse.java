package com.jjt.platform.api.media.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BulkImportJobResponse(
        UUID id,
        String jobType,
        String status,
        int totalFiles,
        int matched,
        int uploaded,
        int skipped,
        int failed,
        List<ImportErrorEntry> errors,
        Instant createdAt,
        Instant completedAt) {

    public record ImportErrorEntry(String filename, String reason) {}
}
