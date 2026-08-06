package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseRequest(
        UUID vendorId,
        String payeeText,
        Integer categoryId,
        UUID costCentreId,
        UUID missionNodeId,
        UUID periodId,
        String invoiceRef,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String currency,
        @NotBlank String description
) {}
