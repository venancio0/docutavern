package com.docutavern.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable @TavernImage annotations.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface TavernImages {
    TavernImage[] value();
}