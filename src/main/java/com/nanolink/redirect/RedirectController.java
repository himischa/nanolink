package com.nanolink.redirect;

import com.nanolink.analytics.ClickEvent;
import com.nanolink.analytics.ClickEventRepository;
import com.nanolink.link.Link;
import com.nanolink.link.LinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RedirectController {
    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;

    public RedirectController(LinkRepository linkRepository, ClickEventRepository clickEventRepository) {
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        Optional<Link> byAlias = linkRepository.findByCustomAlias(code);
        Link link = byAlias.orElseGet(() -> linkRepository.findByShortCode(code).orElse(null));

        if (link == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!link.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (link.isExpired()) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        ClickEvent event = new ClickEvent();
        event.setLink(link);
        event.setIpAddress(request.getRemoteAddr());
        event.setUserAgent(request.getHeader("User-Agent"));
        event.setReferer(request.getHeader("Referer"));
        clickEventRepository.save(event);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(link.getOriginalUrl()))
                .build();
    }
}
