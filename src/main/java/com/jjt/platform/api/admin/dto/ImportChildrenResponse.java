package com.jjt.platform.api.admin.dto;

public record ImportChildrenResponse(
        int totalRows,
        int importedRows,
        int skippedMissingName,
        int skippedMissingCampus,
        int skippedDuplicateRollNumber,
        int generatedRollNumbers,
        int failedRows
) {
}
