package com.jjt.platform.api.auth.dto;

import java.util.UUID;

public record LoginResponse(
    String token,
    String username,
    String role,
    UUID sponsorId
) {
}
