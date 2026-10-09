package com.badminton.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TimeRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeRange {

    String message() default "開始時間は終了時間より前にしてください。";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String startField() default "startTime";

    String endField() default "endTime";
}
