package com.nanolink.analytics;

import com.nanolink.analytics.dto.AnalyticsResponse;
import com.nanolink.common.exception.LinkNotFoundException;
import com.nanolink.link.Link;
import com.nanolink.link.LinkRepository;
import com.nanolink.user.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalyticsService {
    private static final int TOP_LIMIT = 5;

    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(LinkRepository linkRepository, ClickEventRepository clickEventRepository) {
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
    }

    public AnalyticsResponse getAnalytics(UUID linkId, User user) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(LinkNotFoundException::new);

        if (!link.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        long totalClicks = clickEventRepository.countByLinkId(linkId);

        Instant since = Instant.now().minusSeconds(30L * 24 * 60 * 60);
        List<ClickEvent> recentEvents = clickEventRepository.findByLinkIdAndClickedAtAfter(linkId, since);

        List<AnalyticsResponse.DailyClickCount> clicksByDay = recentEvents.stream()
                .collect(Collectors.groupingBy(event ->
                        LocalDate.ofInstant(event.getClickedAt(), ZoneOffset.UTC), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsResponse.DailyClickCount(entry.getKey(), entry.getValue()))
                .toList();

        List<ClickEvent> allEvents = clickEventRepository.findAll().stream()
                .filter(event -> event.getLink().getId().equals(linkId))
                .toList();

        List<AnalyticsResponse.ReferrerCount> topReferrers = allEvents.stream()
                .map(ClickEvent::getReferer)
                .filter(ref -> ref != null && !ref.isBlank())
                .collect(Collectors.groupingBy(ref -> ref, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_LIMIT)
                .map(entry -> new AnalyticsResponse.ReferrerCount(entry.getKey(), entry.getValue()))
                .toList();

        List<AnalyticsResponse.UserAgentCount> topUserAgents = allEvents.stream()
                .map(ClickEvent::getUserAgent)
                .filter(agent -> agent != null && !agent.isBlank())
                .collect(Collectors.groupingBy(agent -> agent, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_LIMIT)
                .map(entry -> new AnalyticsResponse.UserAgentCount(entry.getKey(), entry.getValue()))
                .toList();

        return new AnalyticsResponse(linkId, totalClicks, clicksByDay, topReferrers, topUserAgents);
    }
}
