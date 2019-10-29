package com.nzxpc.handler.util.db.migrate.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IndexColumn {
    /**
     * 索引名称
     */
    String value() default "";

    /**
     * 是否唯一约束
     */
    boolean unique() default false;
}
