package com.docutavern.model;

import com.docutavern.annotations.NoteType; // Importar o Enum

import java.util.Objects;

/**
 * Model representing data from a @TavernParam annotation.
 */
public class ParamModel {
    private final String name;
    private final String description;

    public ParamModel(String name, String description) {
        this.name = Objects.requireNonNull(name, "Parameter name cannot be null");
        this.description = Objects.requireNonNull(description, "Parameter description cannot be null");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Optional: equals, hashCode, toString
}