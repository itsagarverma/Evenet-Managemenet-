package com.sagar.eventmanagement.gallery.storage;

import java.util.Locale;
import java.util.UUID;

public final class GalleryObjectKeys {
    private GalleryObjectKeys() {}

    public static String create(String categorySlug, String extension) {
        if (categorySlug == null || !categorySlug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("Invalid gallery category slug");
        }
        String safeExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (!safeExtension.matches("jpg|png|gif")) {
            throw new IllegalArgumentException("Unsupported gallery image extension");
        }
        return categorySlug + "/" + UUID.randomUUID() + "." + safeExtension;
    }

    public static boolean isSafe(String key) {
        return key != null && key.matches("(?:[a-z0-9]+(?:-[a-z0-9]+)*/)?[0-9a-fA-F-]{36}\\.(?:jpg|png|gif)");
    }
}
