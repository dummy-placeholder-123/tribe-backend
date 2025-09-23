package com.tribe.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Validated
public class JwtProperties {

    /**
     * Shared secret used for signing JWT access tokens.
     */
    @NotBlank(message = "JWT signing secret must not be blank")
    @Size(min = 32, message = "JWT signing secret must be at least 32 characters long")
    private String secret;

    /**
     * Access token validity duration in seconds.
     */
    private long accessTokenTtlSeconds = 900;

    /**
     * Refresh token validity duration in seconds.
     */
    private long refreshTokenTtlSeconds = 604800;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlSeconds;
    }

    public void setRefreshTokenTtlSeconds(long refreshTokenTtlSeconds) {
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }
}
