package com.nanolink.analytics.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AnalyticsResponse {
    private UUID linkId;
    private long totalClicks;
    private List<DailyClickCount> clicksByDay;
    private List<ReferrerCount> topReferrers;
    private List<UserAgentCount> topUserAgents;

    public AnalyticsResponse(
            UUID linkId,
            long totalClicks,
            List<DailyClickCount> clicksByDay,
            List<ReferrerCount> topReferrers,
            List<UserAgentCount> topUserAgents) {
        this.linkId = linkId;
        this.totalClicks = totalClicks;
        this.clicksByDay = clicksByDay;
        this.topReferrers = topReferrers;
        this.topUserAgents = topUserAgents;
    }

    public UUID getLinkId() {
        return linkId;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public List<DailyClickCount> getClicksByDay() {
        return clicksByDay;
    }

    public List<ReferrerCount> getTopReferrers() {
        return topReferrers;
    }

    public List<UserAgentCount> getTopUserAgents() {
        return topUserAgents;
    }

    public record DailyClickCount(LocalDate date, long count) {}

    public record ReferrerCount(String referrer, long count) {}

    public record UserAgentCount(String userAgent, long count) {}
}
