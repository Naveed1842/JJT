package com.jjt.platform.api.admin.dto;

import com.jjt.platform.core.domain.entity.DonorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDonorRequest(
        @NotBlank String displayName,
        String email,
        String phone,
        @NotNull DonorType donorType,
        String notes
) {}
