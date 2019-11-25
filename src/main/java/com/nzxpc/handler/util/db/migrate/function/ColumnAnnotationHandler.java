package com.nzxpc.handler.util.db.migrate.function;

import com.nzxpc.handler.util.db.migrate.model.ColumnModel;

import java.lang.annotation.Annotation;

/**
 * 字段注解处理 仅仅包含一个抽象方法的注解 另外如果什么方法都没有的话，则不是一个函数式接口 其实就相当于传入参数，执行accept对应执行的lanmda方法 跟反射获取字段的util是一个道理
 */
@FunctionalInterface
public interface ColumnAnnotationHandler {
    void accept(Annotation annotation, ColumnModel model);
}
