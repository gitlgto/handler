package com.nzxpc.handler.util;

import com.nzxpc.handler.lang.LangType;
import com.nzxpc.handler.mem.core.entity.Getter;
import com.nzxpc.handler.util.validate.Display;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {

    private static ConcurrentHashMap<Getter<?, ?>, PropDisplayModel> displayMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String[]> _displayMap = new ConcurrentHashMap<>();

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * 检测a能否被b整除 a/b
     *
     * @return
     */
    public static boolean divisible(BigDecimal a, BigDecimal b) {
        try {
            return a.divide(b).scale() <= 0;
        } catch (Throwable e) {
            return false;
        }
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
    public static String rate(BigDecimal value, int scale) {
        if (value == null || value.signum() == 0) {
            return "0.00%";
        }
        DecimalFormat decimalFormat = new DecimalFormat(StringUtils.rightPad("0.00", scale + 2, "#") + "%");
        return decimalFormat.format(value);
    }

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

    /**
     * 让调用线程等待一段时间，只适用于测试环境
     */
    public static void waitFor(long time) {
        long start = System.nanoTime();
        long end;
        do {
            end = System.nanoTime();
        } while (start + time >= end);
    }

    public static String format(boolean value, String trueDisplay, String falseDisplay) {
        if (value) {
            return trueDisplay;
        } else {
            return falseDisplay;
        }
    }
    public static String format(BigDecimal value,boolean byRate,int scale){
        if (byRate){
            return rate(value,scale);
        }else {
            return "";
        }

    }

    /**
     * desc 格式化金额，示例：1=>1.00,1.0=>1.00，0=>0.00,1.21=>1.21
     * 注：一般用于动态控制的小数（如定值定比控制下的小数有时是金额有时是百分数）在列表的显示格式化，其他情况调用format即可
     *
     * @author 钱智慧
     * date 7/4/18 9:23 AM
     **/
    public static String money(BigDecimal money) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        return decimalFormat.format(money);
    }

    /**
     * desc 如果小数位数小于等于2,则按金额初始化（等同于调用Util.money)，否则，按这样的规则格式化：
     * 整数部分每3位加一个逗号，小数部分保留（但末尾的0去掉），示例：
     * 1.0120=>1.012,1234.123=>1,234.123,12.1=>12.10
     *
     * @author 钱智慧
     * date 10/12/18 3:35 PM
     **/
    public static String commaFormat(BigDecimal value) {
        if (value.scale() <= 2) {
            return money(value);
        } else {
            if (value.scale() > 8) {
                value = value.setScale(8, RoundingMode.HALF_EVEN);
            }
            value = value.stripTrailingZeros();
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            decimalFormat.setMaximumFractionDigits(value.scale());
            return decimalFormat.format(value);
        }
    }

    /**
     * desc 功能包括：格式化时间、枚举、boolean、金额、普通的BigDecimal去掉尾部多余的0、格式化float、double
     *
     * @author 钱智慧
     * date 7/4/18 2:37 PM
     **/
//    public static String format(Object obj) {
//        if (obj == null) {
//            return "";
//        }
//
//        if (obj instanceof DayOfWeek) {
//            return Constant.DayOfWeekMap.get(obj);
//        }
//
//        if (obj instanceof Double) {
//            return format(BigDecimal.valueOf((double) obj));
//        }
//
//        if (obj instanceof Float) {
//            return format(BigDecimal.valueOf((float) obj));
//        }
//
//        if (obj instanceof BigDecimal) {
//            return commaFormat((BigDecimal) obj);
//        }
//
//        if (obj instanceof LocalDateTime) {
//            return ((LocalDateTime) obj).format(Constant.YMDHMS);
//        }
//
//        if (obj instanceof LocalDate) {
//            return ((LocalDate) obj).format(Constant.YMD);
//        }
//
//        if (obj instanceof LocalTime) {
//            return ((LocalTime) obj).format(Constant.HMS);
//        }
//
//        if (obj instanceof Enum) {
//            return EnumUtil.getDisplay(obj);
//        }
//
//        if (obj instanceof Boolean) {
//            Boolean b = (Boolean) obj;
//            if (b) {
//                return "是";
//            } else {
//                return "否";
//            }
//        }
//
//        if (obj instanceof List) {
//            List b = (List) obj;
//            return StringUtils.join(b, ",");
//        }
//
//        return obj.toString();
//    }


    @AllArgsConstructor
    private static class PropDisplayModel {
        public String prop;
        public String display;
        public String trueDisplay;
        public String falseDisplay;
    }

    public static String langDisplay(Display display, LangType langType) {
        switch (langType) {
            case ZhCn:
                return display.value();
            case EnUs:
                return display.enValue();
            default:
                return display.value();
        }

    }

    /**
     * 凡是继承了Serializable的函数式接口的实例都可以获取一个属于它的SerializedLambda实例，并且通过它获取到方法的名称，
     * 根据我们标准的java bean的定义规则就可以通过方法名称来获取属性名称，属性名称也就是我们数据库中对应的列了
     *
     * @param getter 继承seralizable
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> SerializedLambda getLanmda(Getter<T, V> getter) {
        try {
            Method method = getter.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda lambda = (SerializedLambda) method.invoke(getter);
            return lambda;
        } catch (Exception e) {
            LogUtil.err("", e);
        }
        return null;
    }

    private static PropDisplayModel display(SerializedLambda lambda, LangType langType) {
        //示例：获取的是实体路径 com.xx.xx.user
        try {
            String replace = lambda.getImplClass().replace("/", ".");
            Class<?> clazz = Class.forName(replace);
            //获取的是对应的方法名
            String methodName = lambda.getImplMethodName();
            //切割方法名，获取字段。例：getUser 获取user
            String filedName = Introspector.decapitalize(methodName.substring(methodName.startsWith("is") ? 2 : 3));
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (StringUtils.equalsIgnoreCase(filedName, field.getName())) {
                        Display display = field.getAnnotation(Display.class);
                        if (display != null) {
                            return new PropDisplayModel(field.getName(), langDisplay(display, langType), display.trueDisplay(), display.falseDisplay());
                        } else {
                            return new PropDisplayModel(field.getName(), field.getName(), "", "");
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            LogUtil.err("", e);
        }
        return null;
    }

    public static <T, V> String prop(Getter<T, V> getter) {
        return displayMap.computeIfAbsent(getter, k -> {
            SerializedLambda lanmda = getLanmda(getter);
            PropDisplayModel model = display(lanmda, LangType.ZhCn);
            return model;
        }).prop;
    }

}
