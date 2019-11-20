package com.nzxpc.handler.util.validate;

import java.math.BigDecimal;

public class ValidateUtil {
    /**
     * @param canBeZero        可否为0
     * @param canBeNegative    可否为负
     * @param value
     * @param scale            小数位数
     * @param mustLessEqualOne 是否必须小于等于1
     * @return
     */
    public static boolean checkDecimal(boolean canBeZero, boolean canBeNegative, BigDecimal value, int scale, boolean mustLessEqualOne) {
        if (value == null) {
            return false;
        }
        if (value.signum() == 0 && !canBeZero) {
            return false;
        }
        if (value.signum() < 0 && !canBeNegative) {
            return false;
        }
        if (value.compareTo(BigDecimal.ONE) > 0 && mustLessEqualOne) {
            return false;
        }
        if (value.scale() > scale) {
            return false;
        } else {
            //数据长度限制 0.000123=>3 0.123=>3 12.123=>5 120.123=>6 1200.123=>7 12.00123=>7 由此可见整数位大于0取所有数，等于0只取不为0的所有
            if (value.precision() > 15 + 13) {
                return false;
            }
            return true;
        }
    }
}
