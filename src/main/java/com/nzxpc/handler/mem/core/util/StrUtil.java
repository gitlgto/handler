package com.nzxpc.handler.mem.core.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.StringJoiner;

public class StrUtil {
    private static final String SEP1 = ",";
    private static final String SEP2 = "=";

    private static String obj2Str(Object obj) {
        StringJoiner stringJoiner = new StringJoiner(SEP1, "{", "}");
        Map<String, Object> map = ReflectUtil.getAllFields(obj);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            String valStr = "";
            if (value instanceof BigDecimal) {
                valStr = String.valueOf(((BigDecimal) value).doubleValue());
            } else {
                valStr = value.toString();
            }
            stringJoiner.add(entry.getKey() + SEP2 + valStr);
        }
        return stringJoiner.toString();
    }
}
