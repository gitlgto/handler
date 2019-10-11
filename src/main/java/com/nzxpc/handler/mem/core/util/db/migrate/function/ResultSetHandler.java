package com.nzxpc.handler.mem.core.util.db.migrate.function;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 查询结果集处理
 * @param <R>
 */
@FunctionalInterface
public interface ResultSetHandler<R> {
    R accept(ResultSet rt) throws SQLException;
}
