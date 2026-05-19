package com.nanolink.link.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.hibernate.validator.constraints.URL;

public class CreateLinkRequest {
    @NotBlank
    @URL
    private String originalUrl;

    private String customAlias;
    private Instant expiresAt;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
