package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
        @NotNull BigDecimal receivedAmount,
        @NotBlank String currency,
        @NotBlank String bankReference,
        @NotNull LocalDate receivedDate
) {}
