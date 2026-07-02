package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record AdminChildSummaryResponse(
        UUID id,
        String rollNumber,
        String fullName,
        String city,
        String campusName,
        String schoolName,
        String educationAmount,
        String educationCurrency,
        String availabilityStatus,
        String currentSponsorName,
        String currentSponsorEmail
) {}
