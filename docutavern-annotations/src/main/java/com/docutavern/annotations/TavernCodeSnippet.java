package com.docutavern.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface TavernCodeSnippet {
    String lang() default "java";
    String value() default "";
    String source() default "";
}