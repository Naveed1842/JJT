package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record RecordEarlySupportRequest(UUID childId, String month, String educationAmount, String educationCurrency, UUID ledgerEntryId) {
}
