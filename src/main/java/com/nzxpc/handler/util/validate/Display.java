package com.nzxpc.handler.util.validate;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Display {
    String value() default "";
    String enValue() default "";

    String trueDisplay() default "";
    String falseDisplay() default "";
}
