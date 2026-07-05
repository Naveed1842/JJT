package com.jjt.platform.api.admin.dto;

import java.util.List;
import java.util.UUID;

public record MonthlyReconciliationResponse(
        int year,
        String month,
        Summary summary,
        List<AtRiskSponsorship> atRisk,
        List<SponsorPaymentResponse> payments
) {
    public record Summary(int expected, int received, int partial, int overdue, int waived, int total) {}

    public record AtRiskSponsorship(
            UUID sponsorshipId,
            UUID sponsorId,
            String sponsorName,
            UUID childId,
            String childName,
            int consecutiveOverdueMonths,
            boolean requiresEscalation
    ) {}
}
