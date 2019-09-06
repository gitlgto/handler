package com.nzxpc.handler.mem.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

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
}
