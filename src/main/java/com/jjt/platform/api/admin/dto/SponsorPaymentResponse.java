package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SponsorPaymentResponse(
        UUID id,
        UUID sponsorshipId,
        UUID sponsorId,
        UUID childId,
        String paymentMonth,
        String status,
        BigDecimal expectedAmount,
        String expectedCurrency,
        BigDecimal receivedAmount,
        String receivedCurrency,
        String bankReference,
        LocalDate receivedDate,
        String waiverReason,
        UUID fundTransactionId,
        UUID ledgerEntryId,
        Instant createdAt,
        Instant updatedAt
) {}
