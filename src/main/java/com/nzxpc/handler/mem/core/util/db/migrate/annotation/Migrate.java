package com.nzxpc.handler.mem.core.util.db.migrate.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Migrate {

    /**
     * 指明旧字段或者旧表名，从而可以做到更改字段或者表名时不丢失旧数据
     */
    String oldName() default "";
}
