package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateOrgConfigRequest(
        @Size(min = 2, max = 255) String name,
        @Pattern(regexp = "[A-Z]{3}") String baseCurrency,
        @Min(1) @Max(28) Integer paymentDueDay,
        @DecimalMin("0.00") BigDecimal minFundReserve
) {}
