package com.jjt.platform.api.common.dto;

import java.util.UUID;

public record LedgerEntryDto(UUID id, String month, String educationAmount, String educationCurrency, String coverageType) {
}
