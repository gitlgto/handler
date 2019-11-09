package com.nzxpc.handler.util.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 自定义注解
 */
public class RateControlValidator implements ConstraintValidator<RateControl, Object> {

    //属性值字段名称数组
    private String[] targetArr;

    //对应定值定比boolean值字段名称数组
    private String[] byArr;

    //为比例可否为0数组
    private boolean[] rateCanBeZero;

    //为普通值可否为0数组
    private boolean[] decimalCanBeZeroArr;

    @Override
    public void initialize(RateControl constraintAnnotation) {
        //初始化方法，进行初始化操作

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        //校验方法（object value对应添加注解的对象，类，字段等）
        return false;
    }
}
