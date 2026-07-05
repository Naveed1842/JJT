package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FundAccountResponse(
        UUID id,
        String name,
        String currency,
        BigDecimal balance,
        BigDecimal minReserve,
        boolean belowMinReserve
) {}
