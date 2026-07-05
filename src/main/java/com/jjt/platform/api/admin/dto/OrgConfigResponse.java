package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrgConfigResponse(
        UUID id,
        String name,
        String slug,
        String baseCurrency,
        int paymentDueDay,
        BigDecimal minFundReserve,
        boolean active,
        Instant createdAt
) {}
