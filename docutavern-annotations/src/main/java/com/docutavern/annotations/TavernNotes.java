package com.docutavern.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable @TavernNote annotations.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface TavernNotes {
    TavernNote[] value();
}