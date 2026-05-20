package com.nanolink.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nanolink.common.exception.AliasAlreadyTakenException;
import com.nanolink.link.dto.CreateLinkRequest;
import com.nanolink.user.User;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;

    private LinkService linkService;

    @BeforeEach
    void setUp() {
        linkService = new LinkService(linkRepository, "http://localhost:8080");
    }

    @Test
    void createLink_withValidData_returnsLinkResponse() {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setOriginalUrl("https://example.com");

        User user = new User();
        user.setId(UUID.randomUUID());

        Link saved = new Link();
        saved.setId(UUID.randomUUID());
        saved.setUser(user);
        saved.setOriginalUrl("https://example.com");
        saved.setShortCode("abc123");

        when(linkRepository.existsByShortCode(any())).thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenReturn(saved);

        var response = linkService.createLink(request, user);
        assertEquals("https://example.com", response.getOriginalUrl());
        assertEquals("abc123", response.getShortCode());
    }

    @Test
    void createLink_withDuplicateAlias_throwsAliasAlreadyTakenException() {
        CreateLinkRequest request = new CreateLinkRequest();
        request.setOriginalUrl("https://example.com");
        request.setCustomAlias("my-alias");

        User user = new User();
        user.setId(UUID.randomUUID());

        when(linkRepository.existsByCustomAlias("my-alias")).thenReturn(true);

        assertThrows(AliasAlreadyTakenException.class, () -> linkService.createLink(request, user));
    }

    @Test
    void createLink_generatesUniqueShortCode_afterCollisions() {
        AtomicInteger calls = new AtomicInteger();
        when(linkRepository.existsByShortCode(any())).thenAnswer(invocation -> calls.getAndIncrement() < 2);

        String code = linkService.generateUniqueShortCode();
        assertNotNull(code);
        assertEquals(6, code.length());
    }

    @Test
    void deleteLink_byAnotherUser_throwsForbidden() {
        UUID linkId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);

        User other = new User();
        other.setId(otherId);

        Link link = new Link();
        link.setId(linkId);
        link.setUser(owner);

        when(linkRepository.findById(linkId)).thenReturn(Optional.of(link));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> linkService.deleteLink(linkId, other));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
