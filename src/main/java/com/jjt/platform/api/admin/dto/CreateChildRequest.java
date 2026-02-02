package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CreateChildRequest(String fullName, String educationAmount, String educationCurrency, UUID childId, UUID ledgerId) {
}
