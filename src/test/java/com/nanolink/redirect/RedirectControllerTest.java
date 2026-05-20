package com.nanolink.redirect;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nanolink.analytics.ClickEventRepository;
import com.nanolink.link.Link;
import com.nanolink.link.LinkRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RedirectController.class)
@AutoConfigureMockMvc(addFilters = false)
class RedirectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinkRepository linkRepository;

    @MockBean
    private ClickEventRepository clickEventRepository;

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
