package com.nzxpc.handler.mem.core.util.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SqlHelper<T> extends NamedParameterJdbcDaoSupport {
    private final static char QuoteMark = '`';
    List<String> propList = new ArrayList<>();
    String tableName;
    Class<T> persistentClass;


    private static String conditionSql(Object inputValue, boolean isIgnore, Map<String, Object> argMap, String srcSql, String conditionSql) {
        String ret;
        if (inputValue == null || isIgnore) {
            ret = srcSql;
        } else {
            ret = conditionSql(srcSql, conditionSql);
            Matcher matcher = Pattern.compile(":(\\w+)").matcher(conditionSql);
            if (matcher.find()) {
                String group = matcher.group(1);
                if (inputValue instanceof Enum<?>) {
                    Enum<?> value = (Enum<?>) inputValue;
                    argMap.put(group, value.ordinal());
                } else {
                    argMap.put(group, inputValue);
                }
            }
        }
        return ret;
    }

    protected NamedParameterJdbcTemplate getJt() {
        return getNamedParameterJdbcTemplate();
    }

    public <PrimitiveType> List<PrimitiveType> simpleList(String sql, Map<String, Object> map, Class<PrimitiveType> primitiveTypeClass) {
        return getJt().queryForList(sql, map, primitiveTypeClass);
    }

    protected abstract PrepareSqlParamResult prepareSqlParamForUpdate(Object bean, List<String> updateColumns);

    protected abstract PrepareSqlParamResult prepareSqlParamForAdd(Object bean);

    public abstract boolean exist(String conditionSql, Map<String, Object> argMap);


    /**
     * 将map中的参数枚举转换成ordinal
     */

    static Map<String, Object> changeEum(Map<String, Object> argMap) {
        Map<String, Object> ret = null;
        if (argMap != null) {
            ret = new HashMap<>();
            for (String s : argMap.keySet()) {
                Object o = argMap.get(s);
                if (o.getClass().isEnum()) {
                    Enum o1 = (Enum) o;
                    int ordinal = o1.ordinal();
                    ret.put(s, ordinal);
                } else {
                    ret.put(s, o);
                }
            }
        }
        return ret;
    }

    /**
     * 执行原生sql进行db修改
     */
    public int doSql(String sql, Map<String, Object> argMap) {
        argMap = changeEum(argMap);
        return getJt().update(sql, argMap);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    protected static class PrepareSqlParamResult {
        protected String sql;
        protected MapSqlParameterSource map;
    }

    private static String conditionSql(String srcSql, String conditionSql) {
        String ret;
        if (StringUtils.containsIgnoreCase(srcSql, "where")) {
            ret = srcSql + "and" + conditionSql;
        } else {
            if (StringUtils.containsIgnoreCase(conditionSql, "where")) {
                ret = srcSql + "" + conditionSql;
            } else {
                ret = srcSql + "where" + conditionSql;
            }
        }
        return ret;
    }
}
