package com.nzxpc.handler.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * 根据BeanUtils源码优化的copy工具类，避免对象键值为null，会把target对应键值赋值为null
 * 1.不用跨过某些属性赋值，因此传入的参数有的不需要。2.另外不需要转换
 */
@SuppressWarnings("ALL")
public class MyBeanUtils extends BeanUtils {

    public static HashMap<PropertyDescriptor, PropertyDescriptor> getPropertyDescriptors(Class<?> sourceClass, Class<?> targetClass) {
        HashMap<PropertyDescriptor, PropertyDescriptor> map = new HashMap<>();
        //遍历获取targetPropertyDescriptor
        //同时获取对应sourcePropertyDescriptor
        PropertyDescriptor[] descriptors = getPropertyDescriptors(targetClass);
        for (PropertyDescriptor descriptor : descriptors) {
            //key存targetPropertyDescriptor目标对象  value存sourcePropertyDescriptor操作对象。两者都供使用
            map.put(descriptor, getPropertyDescriptor(sourceClass, descriptor.getName()));
        }
        return map;
    }

    public static <S, T> void copy(S source, T target, HashMap<PropertyDescriptor, PropertyDescriptor> map, boolean ignoreNull) {
        map.forEach((targetPr, sourcePr) -> {
            Method writeMethod = targetPr.getWriteMethod();
            if (writeMethod != null && sourcePr != null) {
                Method readMethod = sourcePr.getReadMethod();
                if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                    try {
                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                            readMethod.setAccessible(true);
                        }
                        Object value = readMethod.invoke(source);
                        if (ignoreNull&&value == null) {
                            return;
                        }
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        writeMethod.invoke(target, value);
                    } catch (Throwable e) {
                        throw new FatalBeanException("Could not copy property '" + targetPr.getName() + "' from source to target", e);
                    }

                }
            }
        });
    }

    public static <S, T> T copy(S source, Class<T> clazz) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(clazz, "TargetClazz must not be null");
        HashMap<PropertyDescriptor, PropertyDescriptor> descriptors = getPropertyDescriptors(source.getClass(), clazz);
        try {
            //生成无参构造失败则抛出异常，并返回null。
            T target = clazz.getDeclaredConstructor().newInstance();
            copy(source, target, descriptors, true);
            return target;
        } catch (Throwable e) {
            LogUtil.err(MyBeanUtils.class, e);
        }
        return null;
    }
}
