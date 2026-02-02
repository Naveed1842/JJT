package com.jjt.platform.api.admin.dto;

import java.util.UUID;

public record AddProgressRequest(String month, String summary, UUID progressUpdateId) {
}
