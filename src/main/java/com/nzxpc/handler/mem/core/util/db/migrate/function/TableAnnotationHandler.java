package com.nzxpc.handler.mem.core.util.db.migrate.function;

import com.nzxpc.handler.mem.core.util.db.migrate.model.TableModel;

import java.lang.annotation.Annotation;

/**
 * 表注解处理方法
 */
@FunctionalInterface
public interface TableAnnotationHandler {
    void accept(Annotation annotation, TableModel model);
}
