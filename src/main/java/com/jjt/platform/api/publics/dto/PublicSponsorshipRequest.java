package com.jjt.platform.api.publics.dto;

import com.jjt.platform.core.domain.entity.CommitmentType;
import java.util.UUID;

public record PublicSponsorshipRequest(UUID childId, CommitmentType commitmentType, PublicSponsorInfo sponsor) {
}
