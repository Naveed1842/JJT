package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID orgId,
        UUID vendorId,
        String payeeText,
        Integer categoryId,
        UUID costCentreId,
        UUID periodId,
        String invoiceRef,
        BigDecimal amount,
        String currency,
        String description,
        String status,
        LocalDate paidAt,
        UUID txId,
        Instant createdAt
) {}
