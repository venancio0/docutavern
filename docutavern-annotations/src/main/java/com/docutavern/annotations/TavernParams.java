package com.docutavern.annotations;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable @TavernParam annotations.
 * Automatically used by the compiler, usually not directly applied by users.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface TavernParams {
    TavernParam[] value();
}