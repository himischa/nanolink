package com.nanolink.redirect;

import com.nanolink.analytics.ClickEventRepository;
import com.nanolink.link.Link;
import com.nanolink.link.LinkRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {
    private MockMvc mockMvc;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @BeforeEach
    void setUp() {
        RedirectController controller = new RedirectController(linkRepository, clickEventRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void redirect_withValidCode_returns302() throws Exception {
        Link link = new Link();
        link.setId(UUID.randomUUID());
        link.setOriginalUrl("https://example.com");
        link.setShortCode("abc123");

        when(linkRepository.findByCustomAlias("abc123")).thenReturn(Optional.empty());
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(link));

        mockMvc.perform(get("/abc123")).andExpect(status().isFound());
    }

    @Test
    void redirect_withExpiredLink_returns410() throws Exception {
        Link link = new Link();
        link.setId(UUID.randomUUID());
        link.setOriginalUrl("https://example.com");
        link.setShortCode("abc123");
        link.setExpiresAt(Instant.now().minusSeconds(60));

        when(linkRepository.findByCustomAlias("abc123")).thenReturn(Optional.empty());
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(link));

        mockMvc.perform(get("/abc123")).andExpect(status().isGone());
    }

    @Test
    void redirect_withInactiveLink_returns404() throws Exception {
        Link link = new Link();
        link.setId(UUID.randomUUID());
        link.setOriginalUrl("https://example.com");
        link.setShortCode("abc123");
        link.setActive(false);

        when(linkRepository.findByCustomAlias("abc123")).thenReturn(Optional.empty());
        when(linkRepository.findByShortCode("abc123")).thenReturn(Optional.of(link));

        mockMvc.perform(get("/abc123")).andExpect(status().isNotFound());
    }

    @Test
    void redirect_withUnknownCode_returns404() throws Exception {
        when(linkRepository.findByCustomAlias("unknown")).thenReturn(Optional.empty());
        when(linkRepository.findByShortCode("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/unknown")).andExpect(status().isNotFound());
    }
}
