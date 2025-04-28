package com.docutavern.annotations;

import java.lang.annotation.*;

/**
 * Includes an image in the documentation.
 * This annotation is repeatable.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Repeatable(TavernImages.class) // Make it repeatable
public @interface TavernImage {
    /** The path to the image file. Relative paths are often resolved based on build configuration or output structure. Required. */
    String path();

    /** Optional caption displayed below the image. */
    String caption() default "";

    /** Optional alternative text for accessibility (used if the image cannot be displayed). Defaults to caption or path if empty. */
    String altText() default "";

    /** Optional width specification (e.g., "80%", "500px"). Syntax support depends on Markdown renderer. */
    String width() default "";
}