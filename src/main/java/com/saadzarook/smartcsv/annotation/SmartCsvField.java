package com.saadzarook.smartcsv.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SmartCsvField {
    /**
     * The name of the CSV column to map to this field.
     */
    String column();

    /**
     * Custom validation constraints (e.g., regex pattern).
     */
    String validation() default "";

    /**
     * Whether this field is required.
     */
    boolean required() default false;
}
