package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record RecordEarlySupportResponse(UUID ledgerEntryId, UUID ledgerId, String month) {
}
