package com.nzxpc.handler.mem.core.util;

import com.nzxpc.handler.lang.LangType;
import com.nzxpc.handler.mem.core.entity.Getter;
import com.nzxpc.handler.validate.Display;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class Util {

    private static ConcurrentHashMap<Getter<?, ?>, PropDisplayModel> displayMap = new ConcurrentHashMap<>();

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
