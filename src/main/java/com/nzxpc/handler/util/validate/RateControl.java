package com.nzxpc.handler.util.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 * desc 用于定值、定比的控制，如果该注解用在了属性A上，且指定是B属性名称，
 * 则B必须得是一个boolean类型的值，为true表示A是个比例值(限定双端验证都只能输入0到1之间的数)，为false表示A
 * 是个普通的小数值
 * 该@Target 用于指定该注解的应用范围(type接口，类)，此注解应用于 方法、域、构造函数 、入参四个区域
 * 该@Retention指定应用时间 @Constraint指明具体实现的类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RateControlValidator.class)
public @interface RateControl {
    String message() default "{util.validate.RateControl.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] target();
    String[] by();//boolean值，为true target则是比例值，指定属性名称和by名称

    /**
     * 如果是比例的话，可否为0
     */
    boolean[] rateCanBeZero() default true;

    /**
     * 如果是普通小数的话，可否为0
     */
    boolean[] decimalCanBeZero() default true;
}
