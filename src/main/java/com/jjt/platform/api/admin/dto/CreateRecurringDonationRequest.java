package com.jjt.platform.api.admin.dto;

import com.jjt.platform.core.domain.entity.DonationFrequency;
import com.jjt.platform.core.domain.entity.DonationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRecurringDonationRequest(
        @NotNull UUID donorId,
        @NotNull DonationType donationType,
        @NotBlank String amount,
        @NotBlank String currency,
        @NotNull DonationFrequency frequency,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        UUID fundAccountId,
        String notes
) {}
