package com.docutavern.annotations;

/**
 * Defines the type of note/admonition block to be rendered.
 */
public enum NoteType {
    /** General information. */
    NOTE,
    /** Helpful tip or suggestion. */
    TIP,
    /** Important information requiring attention. */
    IMPORTANT,
    /** Warning about potential issues or risks. */
    WARNING,
    /** Indicates danger or critical information. */
    DANGER
}