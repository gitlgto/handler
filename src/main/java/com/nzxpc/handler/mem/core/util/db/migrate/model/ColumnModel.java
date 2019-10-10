package com.nzxpc.handler.mem.core.util.db.migrate.model;

import com.nzxpc.handler.mem.core.util.db.migrate.core.DataType;

public class ColumnModel {
    /**
     * 表名
     */
    public String TABLE_NAME;
    /**
     * 列名
     */
    public String COLUMN_NAME;
    /**
     * 默认值
     */
    public String COLUMN_DEFAULT;
    /**
     * 是否为空 YES | NO
     */
    public String IS_NULLABLE;
    /**
     * 长度 字符型
     */
    public long CHARACTER_MAXIMUM_LENGTH;
    /**
     * 长度 数字型
     */
    public int NUMERIC_PRECISION;
    /**
     * 精度 数字型
     */
    public int NUMERIC_SCALE;
    /**
     * 长度 DATETIME类型
     */
    public int DATETIME_PRECISION;
    /**
     * 列类型
     */
    public String COLUMN_TYPE;

    /**
     * 自增等 [ AUTO_INCREMENT ]
     */
    public String EXTRA;
    /**
     * 注释
     */
    public String COLUMN_COMMENT;
    /**
     * 旧列名, 判断重命名时用到
     */
    public String OLD_COLUMN_NAME;

    /**
     * 类型枚举，方便判断与使用
     */
    private DataType dataType;

    @Override
    public String toString() {
        return super.toString();
    }
}
