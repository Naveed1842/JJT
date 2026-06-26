package com.jjt.platform.api.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType) {

    public static LoginResponse of(String accessToken, String refreshToken, long expiresInMs) {
        return new LoginResponse(accessToken, refreshToken, expiresInMs / 1000L, "Bearer");
    }
}
