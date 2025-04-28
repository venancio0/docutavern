package com.docutavern.model;

import java.util.Objects;

/**
 * Model representing data from a @TavernReturn annotation.
 */
public class ReturnModel {
    private final String description;

    public ReturnModel(String description) {
        this.description = Objects.requireNonNull(description, "Return description cannot be null");
    }

    public String getDescription() {
        return description;
    }

    // Optional: equals, hashCode, toString
}