package com.docutavern.model;

import com.docutavern.annotations.NoteType; // Importar o Enum!

import java.util.Objects;

/**
 * Model representing data from a @TavernNote annotation.
 */
public class NoteModel {
    private final NoteType type;
    private final String title; // Optional
    private final String value;

    public NoteModel(NoteType type, String title, String value) {
        this.type = Objects.requireNonNull(type, "Note type cannot be null");
        this.title = title; // Can be null or empty
        this.value = Objects.requireNonNull(value, "Note value cannot be null");
    }

    public NoteType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    // Optional: equals, hashCode, toString
}