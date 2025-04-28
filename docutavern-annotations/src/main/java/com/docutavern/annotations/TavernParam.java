package com.docutavern.annotations;

import java.lang.annotation.*;

/**
 * Documents a single parameter of a method or constructor.
 * This annotation is repeatable.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Repeatable(TavernParams.class) // Make it repeatable
public @interface TavernParam {
    /** The exact name of the parameter. Required. */
    String name();

    /** A description of the parameter's purpose. Required. */
    String description();
}