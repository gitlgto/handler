package com.nzxpc.handler.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

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

    private static String list2Str(List<?> list) {
        StringJoiner sj = new StringJoiner(SEP1, "[", "]");
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == null || list.get(i) == "") {
                    continue;
                }
                if (list.get(i) instanceof List) {
                    sj.add(list2Str((List<?>) list.get(i)));
                } else {
                    sj.add(obj2Str(list.get(i)));
                }
            }
        }
        return sj.toString();
    }

    /**
     * 将逗号分隔的字符串转换成list
     */
    public static List<String> toStrList(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] split = StringUtils.split(str, ",");
            return Arrays.asList(split);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 将逗号分隔的字符串转换成整数list 只针对数字
     */
    public static List<Integer> toIntList(String str) {
        List<Integer> ret = new ArrayList<>();
        if (StringUtils.isNotBlank(str)) {
            String[] split = StringUtils.split(str, ",");
            Arrays.stream(split).forEach(item -> ret.add(Integer.parseInt(item)));
        }
        return ret;
    }

    /**
     * 前七位是用户id，不足七位在左边补0，后九位是订单id，不足在左边补0
     */
    public static String orderNo(int userId, int orderId) {
        return String.format("%07d%09d", userId, orderId);
    }

    /**
     * 将对象转为string，如{name=rose,age=2}，
     * 如果对象是List&lt;?&gt;则为：[{name=jack,age=10},{name=jack1,age=11}]，
     * 不考虑map类型，不考虑对象内部的对象属性的递归情况，并且假设对象中的属性都是基本类型
     */
    public static String toStr(Object obj) {
        if (obj instanceof List<?>) {
            return list2Str((List<?>) obj);
        } else {
            return obj2Str(obj);
        }
    }
}
