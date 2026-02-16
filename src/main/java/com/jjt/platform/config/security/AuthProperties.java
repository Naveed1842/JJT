package com.jjt.platform.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class AuthProperties {

    private AuthMode mode = AuthMode.DUAL;

    public AuthMode getMode() {
        return mode;
    }

    public void setMode(AuthMode mode) {
        this.mode = mode;
    }
}

