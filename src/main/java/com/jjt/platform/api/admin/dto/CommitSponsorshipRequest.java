package com.jjt.platform.api.admin.dto;

import com.jjt.platform.core.domain.entity.CommitmentType;
import java.util.UUID;

public record CommitSponsorshipRequest(UUID sponsorId,
                                       UUID childId,
                                       String startMonth,
                                       UUID sponsorshipId,
                                       CommitmentType commitmentType) {
}
