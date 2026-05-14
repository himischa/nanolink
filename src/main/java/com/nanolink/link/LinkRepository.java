package com.nanolink.link;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, UUID> {
    Optional<Link> findByShortCode(String shortCode);

    Optional<Link> findByCustomAlias(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    List<Link> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
