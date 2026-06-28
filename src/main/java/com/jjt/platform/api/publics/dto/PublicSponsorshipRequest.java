package com.jjt.platform.api.publics.dto;

import com.jjt.platform.core.domain.entity.CommitmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PublicSponsorshipRequest(
        @NotNull UUID childId,
        @NotNull CommitmentType commitmentType,
        @NotNull @Valid PublicSponsorInfo sponsor
) {}
