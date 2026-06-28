package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateSponsorRequest(
        @NotNull UUID sponsorId,
        @NotBlank String displayName,
        @NotBlank @Email String contactEmail,
        String phone
) {
}
