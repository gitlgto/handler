package com.nzxpc.handler.mem.core.util.db;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import java.util.ArrayList;
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
