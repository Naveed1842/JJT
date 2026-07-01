package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DonationResponse(
        UUID id,
        UUID donorId,
        String donorName,
        String donationType,
        BigDecimal amount,
        String currency,
        LocalDate donationDate,
        String receiptNumber,
        String status,
        UUID fundAccountId,
        UUID fundTransactionId,
        UUID recurringScheduleId,
        Instant createdAt
) {}
