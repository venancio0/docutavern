package com.docutavern.annotations;

import java.lang.annotation.*;

/**
 * Creates a highlighted note block (admonition) in the documentation.
 * This annotation is repeatable.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Repeatable(TavernNotes.class) // Make it repeatable
public @interface TavernNote {
    /** The type of note (determines styling/icon). Required. */
    NoteType type();

    /** An optional title for the note block. */
    String title() default "";

    /** The main content/text of the note. Required. */
    String value();
}