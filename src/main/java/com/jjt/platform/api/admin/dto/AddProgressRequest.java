package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AddProgressRequest(
        @NotBlank String month,
        @NotBlank String summary,
        UUID progressUpdateId) {
}
