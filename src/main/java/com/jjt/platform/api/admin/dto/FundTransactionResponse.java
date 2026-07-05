package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FundTransactionResponse(
        UUID id,
        UUID fundAccountId,
        String transactionType,
        BigDecimal amount,
        String currency,
        String reason,
        String description,
        String externalReference,
        UUID ledgerEntryId,
        UUID createdBy,
        Instant createdAt
) {}
