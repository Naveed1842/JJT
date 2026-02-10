package com.jjt.platform.api.auth.dto;

public record RegisterRequest(
    String username,
    String password,
    String email,
    String role
) {
}
