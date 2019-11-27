package com.nzxpc.handler.util.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行有效比例验证，正数，且小于等于1，小数位不超过四位，
 * canBeOverOne 是否可以超过1，默认false
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveRateValidator.class)
public @interface PositiveRate {
    String message() default "util.validate.positiveRate.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean canBeZero() default false;

    boolean canBeOverOne() default false;
}
