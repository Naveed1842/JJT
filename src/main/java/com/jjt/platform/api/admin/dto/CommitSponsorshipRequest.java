package com.jjt.platform.api.admin.dto;

import com.jjt.platform.core.domain.entity.CommitmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommitSponsorshipRequest(
        @NotNull UUID sponsorId,
        @NotNull UUID childId,
        @NotBlank String startMonth,
        UUID sponsorshipId,
        @NotNull CommitmentType commitmentType) {
}
