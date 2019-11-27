package com.nzxpc.handler.util.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行有效非负小数验证，scale 最大小数位数默认2，canBeZero是否包含0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveDecimalValidator.class)
public @interface PositiveDecimal {
    String message() default "util.validate.positiveDecimal.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean canBeZero() default false;

    int scale() default 2;
}
