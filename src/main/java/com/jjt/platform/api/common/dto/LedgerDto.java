package com.jjt.platform.api.common.dto;

import java.util.List;
import java.util.UUID;

public record LedgerDto(UUID childId, List<LedgerEntryDto> entries) {
}
