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
        targetArr = constraintAnnotation.target();
        byArr = constraintAnnotation.by();
        rateCanBeZero = constraintAnnotation.rateCanBeZero();
        decimalCanBeZeroArr = constraintAnnotation.decimalCanBeZero();

    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        //校验方法（object value对应添加注解的对象，类，字段等）
        return false;
    }

    private boolean canBeZero(boolean byRate, int index) {
        //是否是比例
        try {
            if (byRate) {
                //如果length只有2，却要取角标为2，会报错3.判断是否小于index
                return rateCanBeZero.length == 1 ? rateCanBeZero[0] : rateCanBeZero[index];
            } else {
                return decimalCanBeZeroArr.length == 1 ? decimalCanBeZeroArr[0] : decimalCanBeZeroArr[index];
            }
        } catch (Exception e) {
            throw new RuntimeException("Array IndexOutOf Bounds");
        }
    }
}
