package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record AddProgressResponse(UUID progressUpdateId, UUID childId, String month) {
}
