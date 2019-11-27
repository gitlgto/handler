package com.nzxpc.handler.util.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 进行有效金额验证，正数，且小数位不超过两位
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveMoneyValidator.class)
public @interface PositiveMoney {
    String messsage() default "{util.validate.positiveMoney.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean canBeZero() default false;

}
