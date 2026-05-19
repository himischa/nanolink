package com.nanolink.link;

import com.nanolink.link.dto.CreateLinkRequest;
import com.nanolink.link.dto.LinkResponse;
import com.nanolink.link.dto.UpdateLinkRequest;
import com.nanolink.user.User;
import com.nanolink.user.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService linkService;
    private final UserRepository userRepository;

    public LinkController(LinkService linkService, UserRepository userRepository) {
        this.linkService = linkService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        User user = getCurrentUser();
        LinkResponse response = linkService.createLink(request, user);
        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(response.getId()).toUri())
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getLinks() {
        User user = getCurrentUser();
        return ResponseEntity.ok(linkService.getUserLinks(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkResponse> getLink(@PathVariable UUID id) {
        User user = getCurrentUser();
        return ResponseEntity.ok(linkService.getLinkById(id, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LinkResponse> updateLink(
            @PathVariable UUID id,
            @RequestBody UpdateLinkRequest request) {
        User user = getCurrentUser();
        return ResponseEntity.ok(linkService.updateLink(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLink(@PathVariable UUID id) {
        User user = getCurrentUser();
        linkService.deleteLink(id, user);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}
