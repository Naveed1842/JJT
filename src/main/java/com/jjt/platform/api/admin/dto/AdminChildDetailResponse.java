package com.jjt.platform.api.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminChildDetailResponse(
        UUID id,
        String rollNumber,
        String fullName,
        String city,
        String campusName,
        String schoolName,
        String educationAmount,
        String educationCurrency,
        String availabilityStatus,
        List<SponsorshipItem> sponsorshipHistory,
        List<LedgerItem> ledgerEntries,
        List<ProgressItem> progressUpdates
) {
    public record SponsorshipItem(
            UUID id,
            String sponsorName,
            String sponsorEmail,
            String startMonth,
            String status,
            String commitmentType,
            Instant createdAt
    ) {}

    public record LedgerItem(
            UUID id,
            String month,
            String amount,
            String currency,
            String coverageType
    ) {}

    public record ProgressItem(
            UUID id,
            String month,
            String summary
    ) {}
}
