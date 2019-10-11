package com.nzxpc.handler.mem.core.util.db.migrate.model;

/**
 * 索引
 */
public class KeyModel {
    /** 表名 */
    public String Table;

    /** 是否是唯一约束 */
    public boolean Non_unique;

    /** 索引名称 */
    public String Key_name;

    /** 字段在索引中的顺序 */
    public int Seq_in_index;

    /** 字段名 */
    public String Column_name;
}
