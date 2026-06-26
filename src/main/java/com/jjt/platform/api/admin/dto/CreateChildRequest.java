package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateChildRequest(
        @NotBlank String rollNumber,
        @NotBlank String fullName,
        @NotBlank String city,
        @NotBlank String campusName,
        @NotBlank String schoolName,
        @NotBlank String educationAmount,
        @NotBlank String educationCurrency,
        @NotNull UUID childId,
        @NotNull UUID ledgerId) {
}
