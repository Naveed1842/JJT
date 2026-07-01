package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringDonationResponse(
        UUID id,
        UUID donorId,
        String donorName,
        String donationType,
        BigDecimal amount,
        String currency,
        String frequency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextDueDate,
        String status,
        Instant createdAt
) {}
