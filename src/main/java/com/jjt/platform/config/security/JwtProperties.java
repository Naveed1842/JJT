package com.jjt.platform.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiryMs = 900_000L;
    private long refreshTokenExpiryMs = 604_800_000L;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTokenExpiryMs() { return accessTokenExpiryMs; }
    public void setAccessTokenExpiryMs(long accessTokenExpiryMs) { this.accessTokenExpiryMs = accessTokenExpiryMs; }

    public long getRefreshTokenExpiryMs() { return refreshTokenExpiryMs; }
    public void setRefreshTokenExpiryMs(long refreshTokenExpiryMs) { this.refreshTokenExpiryMs = refreshTokenExpiryMs; }
}
