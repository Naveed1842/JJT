package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CreateSponsorResponse(
        UUID sponsorId,
        String displayName,
        String contactEmail
) {
}
