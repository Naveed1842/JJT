package com.jjt.platform.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record WaivePaymentRequest(@NotBlank String reason) {}
