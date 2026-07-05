package com.jjt.platform.api.admin.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        List<FundSummary> fundAccounts,
        ChildrenStats childrenStats,
        PaymentStats paymentStats,
        int activeAlertCount
) {
    public record FundSummary(
            UUID id,
            String name,
            BigDecimal balance,
            String currency,
            boolean belowMinReserve
    ) {}

    public record ChildrenStats(
            int total,
            int availableCount,
            int enrolledCount
    ) {}

    public record PaymentStats(
            int year,
            String month,
            int expected,
            int received,
            int overdue,
            int waived,
            int total
    ) {}
}
