package com.nzxpc.handler.mem.core.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 以百分比形式输出，输出形式：0.2345=>23.54%，0.1=>10.00%,
     * 若超过了4位小数，则四舍五入
     */
    public static String rate(BigDecimal bd) {
        if (bd == null || bd.signum() == 0) {
            return "0.00%";
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        return decimalFormat.format(bd);
    }
    /**
     * 最大精度 0.2233555=> 22.33% 超过四舍五入 精度四一下都是两位,往上都是四舍五入取精度
     */




    /**
     * 使用场景，按指定场景去重
     */
    public static <T> Predicate<T> distinctBy(Function<? super T, ?> key) {
        Set<Object> set = ConcurrentHashMap.newKeySet();
        return a -> set.add(key.apply(a));
    }

    public static String err(BindingResult result) {
        FieldError error = result.getFieldError();
        if (error != null) {
            return error.getDefaultMessage();
        } else {
            return "未知错误";
        }
    }

    /**
     * 判断BigDecimal是不是整数
     */
    public static boolean isInt(BigDecimal bigDecimal) {
        return bigDecimal.stripTrailingZeros().scale() <= 0;
    }

    public static String format(boolean value, String trueDisplay, String falseDisplay) {
        if (value) {
            return trueDisplay;
        } else {
            return falseDisplay;
        }
    }

}
