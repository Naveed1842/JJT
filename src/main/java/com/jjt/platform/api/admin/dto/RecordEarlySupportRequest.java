package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecordEarlySupportRequest(
        @NotNull UUID childId,
        @NotBlank String month,
        @NotBlank String educationAmount,
        @NotBlank String educationCurrency,
        UUID ledgerEntryId,
        boolean force,
        String forceReason) {
}
