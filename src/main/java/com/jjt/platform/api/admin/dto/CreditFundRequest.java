package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreditFundRequest(
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String description,
        String externalReference
) {}
