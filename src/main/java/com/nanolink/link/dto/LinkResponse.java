package com.nanolink.link.dto;

import java.time.Instant;
import java.util.UUID;

public class LinkResponse {
    private UUID id;
    private String shortUrl;
    private String originalUrl;
    private String shortCode;
    private String customAlias;
    private Instant expiresAt;
    private boolean isActive;
    private Instant createdAt;

    public LinkResponse(
            UUID id,
            String shortUrl,
            String originalUrl,
            String shortCode,
            String customAlias,
            Instant expiresAt,
            boolean isActive,
            Instant createdAt) {
        this.id = id;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.customAlias = customAlias;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
