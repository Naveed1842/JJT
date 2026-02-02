package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record CreateChildResponse(UUID childId, UUID ledgerId) {
}
