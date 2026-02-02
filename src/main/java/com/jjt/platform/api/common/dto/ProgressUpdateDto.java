package com.jjt.platform.api.common.dto;

import java.util.UUID;

public record ProgressUpdateDto(UUID id, String month, String summary) {
}
