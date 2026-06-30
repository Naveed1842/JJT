package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FundBalanceResponse(
        UUID fundAccountId,
        String fundName,
        String currency,
        BigDecimal balance,
        BigDecimal minReserve,
        boolean belowMinReserve
) {}
