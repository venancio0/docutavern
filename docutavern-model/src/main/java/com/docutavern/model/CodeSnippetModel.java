package com.docutavern.model;

import java.util.Objects;

/**
 * Represents a code snippet to be included in the documentation.
 */
public class CodeSnippetModel {
    private final String language;
    private final String content;
    private final String sourcePath;
    private final String sourceElement;

    public CodeSnippetModel(String language, String content, String sourcePath, String sourceElement) {
        this.language = language != null ? language : "plaintext";
        this.content = Objects.requireNonNull(content, "Snippet content cannot be null");
        this.sourcePath = sourcePath; // Can be null if inline
        this.sourceElement = Objects.requireNonNull(sourceElement, "Snippet source element cannot be null");
    }

    public String getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getSourceElement() {
        return sourceElement;
    }

    // equals, hashCode, toString (optional)
    @Override
    public String toString() {
        return "CodeSnippetModel{" +
                "language='" + language + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", sourceElement='" + sourceElement + '\'' +
                '}';
    }
}