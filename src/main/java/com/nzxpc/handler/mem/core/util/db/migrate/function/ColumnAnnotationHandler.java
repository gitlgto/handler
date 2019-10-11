package com.nzxpc.handler.mem.core.util.db.migrate.function;

import com.nzxpc.handler.mem.core.util.db.migrate.model.ColumnModel;

import java.lang.annotation.Annotation;

/**
 * 字段注解处理
 */
@FunctionalInterface
public interface ColumnAnnotationHandler {
    void accept(Annotation annotation, ColumnModel model);
}
