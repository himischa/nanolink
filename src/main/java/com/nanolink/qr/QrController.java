package com.nanolink.qr;

import com.google.zxing.WriterException;
import com.nanolink.common.exception.LinkNotFoundException;
import com.nanolink.link.Link;
import com.nanolink.link.LinkRepository;
import com.nanolink.user.User;
import com.nanolink.user.UserRepository;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/links")
public class QrController {
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final QrService qrService;
    private final String appBaseUrl;

    public QrController(
            LinkRepository linkRepository,
            UserRepository userRepository,
            QrService qrService,
            @Value("${app.base-url}") String appBaseUrl) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.qrService = qrService;
        this.appBaseUrl = appBaseUrl;
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable UUID id) {
        User user = getCurrentUser();
        Link link = linkRepository.findById(id).orElseThrow(LinkNotFoundException::new);
        if (!link.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        String shortUrl = appBaseUrl + "/" + link.getActiveCode();
        try {
            byte[] png = qrService.generateQrCode(shortUrl);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
        } catch (WriterException | IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "QR generation failed");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}
