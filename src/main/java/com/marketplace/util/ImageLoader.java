package com.marketplace.util;

import javafx.scene.image.Image;

import java.util.concurrent.ConcurrentHashMap;

public class ImageLoader {
    private static final ConcurrentHashMap<String, Image> cache = new ConcurrentHashMap<>();
    private static final Image PLACEHOLDER = new Image(
            ImageLoader.class.getResourceAsStream("/com/marketplace/images/placeholder.png"), 300, 300, true, true);

    public static Image load(String url) {
        if (url == null || url.isEmpty()) return PLACEHOLDER;
        return cache.computeIfAbsent(url, u -> {
            try {
                Image img = new Image(u, 300, 300, true, true, true);
                img.errorProperty().addListener((obs, old, err) -> {
                    if (err) cache.remove(u);
                });
                return img;
            } catch (Exception e) {
                return PLACEHOLDER;
            }
        });
    }

    public static Image loadLarge(String url) {
        if (url == null || url.isEmpty()) return PLACEHOLDER;
        try {
            return new Image(url, 600, 600, true, true, true);
        } catch (Exception e) {
            return PLACEHOLDER;
        }
    }

    public static Image getPlaceholder() { return PLACEHOLDER; }

    public static void clearCache() { cache.clear(); }
}
