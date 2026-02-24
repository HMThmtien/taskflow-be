package com.taskflow.taskflow_be.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taskflow.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenMinutes = 120;
    private long refreshTtl;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTokenMinutes() { return accessTokenMinutes; }
    public void setAccessTokenMinutes(long accessTokenMinutes) {
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public long getRefreshTtl() { return refreshTtl; }
    public void setRefreshTtl(long refreshTtl) {
        this.refreshTtl = refreshTtl;
    }
}