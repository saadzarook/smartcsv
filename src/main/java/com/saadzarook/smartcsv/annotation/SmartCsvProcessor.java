package com.saadzarook.smartcsv.annotation;

import com.saadzarook.smartcsv.validator.DefaultHeaderValidator;
import com.saadzarook.smartcsv.validator.HeaderValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SmartCsvProcessor {
    /**
     * Specifies the class type of the model to map CSV records to.
     */
    Class<?> model();

    /**
     * Specifies the validation strategy:
     * - "skip": Skip invalid records.
     * - "stop": Stop processing on first error.
     * - "collect": Collect all errors and return them.
     */
    String validationStrategy() default "skip";

    /**
     * Allows users to define custom header validations.
     */
    Class<? extends HeaderValidator> headerValidator() default DefaultHeaderValidator.class;
}
