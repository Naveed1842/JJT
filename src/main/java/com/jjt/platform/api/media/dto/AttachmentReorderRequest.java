package com.jjt.platform.api.media.dto;

import java.util.UUID;

public record AttachmentReorderRequest(UUID id, int sortOrder) {}
