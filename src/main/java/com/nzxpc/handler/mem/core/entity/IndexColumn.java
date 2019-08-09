package com.nzxpc.handler.mem.core.entity;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IndexColumn {
    String value() default "";

    boolean unique() default false;
}
