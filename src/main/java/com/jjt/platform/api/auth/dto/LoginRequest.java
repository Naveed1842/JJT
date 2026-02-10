package com.jjt.platform.api.auth.dto;

public record LoginRequest(
    String username,
    String password
) {
}
