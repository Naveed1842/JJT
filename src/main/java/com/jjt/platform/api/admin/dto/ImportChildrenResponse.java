package com.jjt.platform.api.admin.dto;

import java.util.List;
import java.util.UUID;

public record ImportChildrenResponse(
        int totalRows,
        int importedRows,
        int skippedMissingName,
        int skippedMissingCampus,
        int skippedDuplicateRollNumber,
        int generatedRollNumbers,
        int failedRows,
        List<RowResult> rows
) {
    public record RowResult(int rowNumber, String status, String rollNumber, String fullName, String reason, UUID childId) {}
}
