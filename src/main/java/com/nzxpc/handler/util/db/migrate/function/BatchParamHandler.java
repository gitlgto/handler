package com.nzxpc.handler.util.db.migrate.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 批量执行的sql参数添加
 * @param <T>
 */
@FunctionalInterface
public interface BatchParamHandler<T> {
    void accept(PreparedStatement ps,T t)throws SQLException;
}
