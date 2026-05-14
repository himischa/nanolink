package com.nanolink.analytics;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickEventRepository extends JpaRepository<ClickEvent, UUID> {
    long countByLinkId(UUID linkId);

    List<ClickEvent> findByLinkIdAndClickedAtAfter(UUID linkId, Instant since);
}
