package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CommitSponsorshipRequest(UUID sponsorId, UUID childId, String startMonth, UUID sponsorshipId) {
}
