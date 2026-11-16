package com.zeeyeh.quyvera.plugin.service.plugin.library;

import java.net.URI;
import java.util.Objects;

/**
 * 远程仓库坐标
 * @author Qinloren
 * @date 2026/11/15 13:12
 */
public record LibraryRepository(
        String id,
        URI url
) {

    public LibraryRepository {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Repository id cannot be empty");
        }

        Objects.requireNonNull(url, "Repository URL cannot be null");

        if (!url.isAbsolute()
                || !"https".equalsIgnoreCase(url.getScheme())
                || url.getUserInfo() != null
                || url.getQuery() != null
                || url.getFragment() != null) {
            throw new IllegalArgumentException("Invalid repository URL: %s".formatted(url));
        }
    }
}