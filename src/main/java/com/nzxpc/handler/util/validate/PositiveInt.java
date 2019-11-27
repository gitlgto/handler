package com.nzxpc.handler.util.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行有效正整数和非负整数验证，canBeZero是否包含0。正整数大于0的整数（1234）非负整数（01234）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveIntValidator.class)
public @interface PositiveInt {
    String message() default "util.validate.positiveInt.message";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean canBeZero() default false;
}
