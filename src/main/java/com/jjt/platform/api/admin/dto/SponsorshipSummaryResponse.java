package com.jjt.platform.api.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record SponsorshipSummaryResponse(UUID sponsorshipId,
                                         UUID childId,
                                         UUID sponsorId,
                                         String sponsorName,
                                         String sponsorEmail,
                                         String sponsorPhone,
                                         String commitmentType,
                                         String startMonth,
                                         String status,
                                         Instant createdAt) {
}
