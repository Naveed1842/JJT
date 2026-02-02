package com.jjt.platform.config.security;

public enum Role {
    JJT_ADMIN,
    ORG_ADMIN,
    SPONSOR;

    public static Role fromHeader(String header) {
        if (header == null || header.isBlank()) return null;
        try {
            return Role.valueOf(header.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
