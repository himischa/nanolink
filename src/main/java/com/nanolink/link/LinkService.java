package com.nanolink.link;

import com.nanolink.common.exception.AliasAlreadyTakenException;
import com.nanolink.common.exception.LinkNotFoundException;
import com.nanolink.common.util.ShortCodeGenerator;
import com.nanolink.link.dto.CreateLinkRequest;
import com.nanolink.link.dto.LinkResponse;
import com.nanolink.link.dto.UpdateLinkRequest;
import com.nanolink.user.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LinkService {
    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");
    private static final int ALIAS_MAX_LENGTH = 50;
    private static final int SHORT_CODE_RETRY_LIMIT = 5;

    private final LinkRepository linkRepository;
    private final String appBaseUrl;

    public LinkService(LinkRepository linkRepository, @Value("${app.base-url}") String appBaseUrl) {
        this.linkRepository = linkRepository;
        this.appBaseUrl = appBaseUrl;
    }

    public LinkResponse createLink(CreateLinkRequest request, User user) {
        String customAlias = normalizeAlias(request.getCustomAlias());
        if (customAlias != null) {
            validateAlias(customAlias);
            if (linkRepository.existsByCustomAlias(customAlias)) {
                throw new AliasAlreadyTakenException();
            }
        }

        String shortCode = generateUniqueShortCode();

        Link link = new Link();
        link.setUser(user);
        link.setOriginalUrl(request.getOriginalUrl());
        link.setShortCode(shortCode);
        link.setCustomAlias(customAlias);
        link.setExpiresAt(request.getExpiresAt());

        Link saved = linkRepository.save(link);
        return toResponse(saved);
    }

    public List<LinkResponse> getUserLinks(User user) {
        return linkRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public LinkResponse getLinkById(UUID id, User user) {
        Link link = linkRepository.findById(id)
                .orElseThrow(LinkNotFoundException::new);

        enforceOwnership(link, user);
        return toResponse(link);
    }

    public LinkResponse updateLink(UUID id, UpdateLinkRequest request, User user) {
        Link link = linkRepository.findById(id)
                .orElseThrow(LinkNotFoundException::new);

        enforceOwnership(link, user);

        if (request.getCustomAlias() != null) {
            String customAlias = normalizeAlias(request.getCustomAlias());
            if (customAlias == null) {
                link.setCustomAlias(null);
            } else {
                validateAlias(customAlias);
                linkRepository.findByCustomAlias(customAlias)
                        .filter(existing -> !existing.getId().equals(link.getId()))
                        .ifPresent(existing -> {
                            throw new AliasAlreadyTakenException();
                        });
                link.setCustomAlias(customAlias);
            }
        }

        if (request.getExpiresAt() != null) {
            link.setExpiresAt(request.getExpiresAt());
        }

        if (request.getIsActive() != null) {
            link.setActive(request.getIsActive());
        }

        Link saved = linkRepository.save(link);
        return toResponse(saved);
    }

    public void deleteLink(UUID id, User user) {
        Link link = linkRepository.findById(id)
                .orElseThrow(LinkNotFoundException::new);

        enforceOwnership(link, user);
        linkRepository.delete(link);
    }

    private void enforceOwnership(Link link, User user) {
        if (!link.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    String generateUniqueShortCode() {
        for (int i = 0; i < SHORT_CODE_RETRY_LIMIT; i++) {
            String code = ShortCodeGenerator.generate();
            if (!linkRepository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new RuntimeException("Could not generate unique short code");
    }

    private void validateAlias(String alias) {
        if (alias.length() > ALIAS_MAX_LENGTH || !ALIAS_PATTERN.matcher(alias).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid alias");
        }
    }

    private String normalizeAlias(String alias) {
        if (alias == null) {
            return null;
        }
        String trimmed = alias.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LinkResponse toResponse(Link link) {
        String shortUrl = appBaseUrl + "/" + link.getActiveCode();
        return new LinkResponse(
                link.getId(),
                shortUrl,
                link.getOriginalUrl(),
                link.getShortCode(),
                link.getCustomAlias(),
                link.getExpiresAt(),
                link.isActive(),
                link.getCreatedAt());
    }
}
