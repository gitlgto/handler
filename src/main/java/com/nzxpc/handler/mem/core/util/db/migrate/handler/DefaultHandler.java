package com.nzxpc.handler.mem.core.util.db.migrate.handler;

import com.nzxpc.handler.mem.core.entity.IndexColumn;
import com.nzxpc.handler.mem.core.util.db.migrate.core.MigrateCore;
import com.nzxpc.handler.mem.core.util.db.migrate.model.ColumnModel;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import java.lang.annotation.Annotation;

public class DefaultHandler {
    private static void ColumnHandler(Annotation annotation, ColumnModel model) {
        Column column = (Column) annotation;
        if (StringUtils.isNotBlank(column.name())) {
            model.COLUMN_NAME = column.name();
        }
        if (StringUtils.isNotBlank(column.columnDefinition())) {
            model.COLUMN_TYPE = column.columnDefinition();
        }
        model.IS_NULLABLE = column.nullable() ? "YES" : "NO";
        // 字符类型 长度
        model.CHARACTER_MAXIMUM_LENGTH = column.length();
        // 数字类型 长度
        model.NUMERIC_PRECISION = column.precision();
        // 浮点型的小数位数
        model.NUMERIC_SCALE = column.scale();
        if (column.unique()) {
            model.getTableModel().addKey(null, true, model.COLUMN_NAME);
        }

    }

    private static void IndexColumnHandler(Annotation annotation, ColumnModel model) {
        IndexColumn indexColumn = (IndexColumn) annotation;
        model.getTableModel().addKey(indexColumn.value(), indexColumn.unique(), model.COLUMN_NAME);
    }

    /**
     * 对于添加的接口，为什么接收的是方法也可以,这样在执行接口中accept的方法时，应该是相当于传入参数，然后执行这边对应的方法。另一种子类实现接口，
     * 子类给出具体方法实现，接收参数为接口，调用接口中方法，相当于执行了子类的实现方法中的内容。
     * 至于如何执行是哪个handler，是根据传入的class做比较。
     */
    public static void register() {
        MigrateCore.addColumnAnnotationHandler(Column.class, DefaultHandler::ColumnHandler);
        MigrateCore.addColumnAnnotationHandler(IndexColumn.class, DefaultHandler::IndexColumnHandler);
    }
}
