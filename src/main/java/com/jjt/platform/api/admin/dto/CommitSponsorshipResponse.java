package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CommitSponsorshipResponse(UUID sponsorshipId, UUID sponsorId, UUID childId, String startMonth) {
}
