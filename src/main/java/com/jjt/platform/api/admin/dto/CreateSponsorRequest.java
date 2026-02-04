package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CreateSponsorRequest(
        UUID sponsorId,
        String displayName,
        String contactEmail
) {
}
