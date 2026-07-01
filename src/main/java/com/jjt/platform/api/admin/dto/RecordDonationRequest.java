package com.jjt.platform.api.admin.dto;

import com.jjt.platform.core.domain.entity.DonationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record RecordDonationRequest(
        UUID donorId,
        @NotNull DonationType donationType,
        @NotBlank String amount,
        @NotBlank String currency,
        @NotNull LocalDate donationDate,
        String notes,
        UUID fundAccountId
) {}
