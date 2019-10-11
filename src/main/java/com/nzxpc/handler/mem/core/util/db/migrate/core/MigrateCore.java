package com.nzxpc.handler.mem.core.util.db.migrate.core;

import com.nzxpc.handler.mem.core.util.db.migrate.function.ColumnAnnotationHandler;
import com.nzxpc.handler.mem.core.util.db.migrate.function.TableAnnotationHandler;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 迁移主类
 */
public class MigrateCore {
    private final static LinkedHashMap<Class<? extends Annotation>, ColumnAnnotationHandler> COLUMN_ANNOTATION_HANDLER_MAP = new LinkedHashMap<>();
    private final static LinkedHashMap<Class<? extends Annotation>, TableAnnotationHandler> TABLE_ANNOTATION_HANDLER_MAP = new LinkedHashMap<>();
    private final static Map<Class, DataType> DATA_TYPE_MAP = new HashMap<>();

    public static <T extends Annotation> void addColumnAnnotationHandler(Class<T> clazz, ColumnAnnotationHandler handler) {
        COLUMN_ANNOTATION_HANDLER_MAP.put(clazz, handler);
    }

    public static <T extends Annotation> void addTableAnnotationHandler(Class<T> clazz, TableAnnotationHandler handler) {
        TABLE_ANNOTATION_HANDLER_MAP.put(clazz, handler);
    }
}
