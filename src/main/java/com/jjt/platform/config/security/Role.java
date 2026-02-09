package com.jjt.platform.config.security;

import org.apache.commons.lang3.StringUtils;

public enum Role {
    JJT_ADMIN,
    ORG_ADMIN,
    SPONSOR;

    public static Role fromHeader(String header) {
        if (StringUtils.isBlank(header)) {
            return null;
        }
        
        try {
            return Role.valueOf(header.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
