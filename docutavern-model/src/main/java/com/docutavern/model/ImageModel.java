package com.docutavern.model;

import java.util.Objects;

/**
 * Model representing data from a @TavernImage annotation.
 */
public class ImageModel {
    private final String path;
    private final String caption;
    private final String altText;
    private final String width;

    public ImageModel(String path, String caption, String altText, String width) {
        this.path = Objects.requireNonNull(path, "Image path cannot be null");
        this.caption = caption;
        // Default alt text if not provided
        this.altText = (altText != null && !altText.trim().isEmpty()) ? altText : (caption != null && !caption.trim().isEmpty() ? caption : path);
        this.width = width;
    }

    public String getPath() {
        return path;
    }

    public String getCaption() {
        return caption;
    }

    public String getAltText() {
        return altText;
    }

    public String getWidth() {
        return width;
    }

    // Optional: equals, hashCode, toString
}