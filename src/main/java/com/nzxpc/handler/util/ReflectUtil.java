package com.nzxpc.handler.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.persistence.EmbeddedId;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReflectUtil {
    public static void scanClasses(String packageName, Class<?> baseClazz, Function<Class<?>, Boolean> fn, Class<?>... ignoredClasses) {
        try {
            ClassPathScanningCandidateComponentProvider cr = new ClassPathScanningCandidateComponentProvider(true);
            if (baseClazz == null) {
                baseClazz = Object.class;
            }
            cr.addIncludeFilter(new AssignableTypeFilter(baseClazz));
            Set<Class<?>> set = Set.of(ignoredClasses);
            for (BeanDefinition bd : cr.findCandidateComponents(packageName)) {
                Class<?> name = Class.forName(bd.getBeanClassName());
                if (!set.contains(name)) {
                    if (fn != null && !fn.apply(name)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * lanmda表达式获取字段，调用代码块中的逻辑进行处理 放在lan代码块中处理
     */
    public static void reflectAllField(Class<?> clazz, Consumer<Field> fieldConsumer) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                fieldConsumer.accept(field);
            }
        }
    }

    public static void reflectAllFields(Class<?> clazz, Function<Field, Boolean> fn) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Boolean apply = fn.apply(field);
                if (!apply) {
                    break;
                }
            }
        }
    }

    public static Field getField(Class<?> aClass, String name) {
        final Field[] ret = {null};
        reflectAllFields(aClass, field -> {
                    if (field.getName().equals(name)) {
                        ret[0] = field;
                        return false;
                    }
                    return true;
                }
        );
        return ret[0];
    }

    /**
     * 反射获取对象属性值，包括所有上级类，其中枚举取ordinal值  忽略静态属性和类型非bigdecimal的null值
     */
    public static Map<String, Object> getAllFields(Object bean, List<String> targetPropList) {
        Map<String, Object> ret = new HashMap<>();
        reflectAllField(bean.getClass(), field -> {
            if (ret.containsKey(field.getName())) {
                return;//继承如果存在同名的属性，则通过反射从父类获取的是null
            }
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            if (isStatic) {
                return;//忽略静态属性
            }
            boolean isOk = targetPropList == null;
            Optional<String> item = Optional.empty();
            if (!isOk) {
                item = targetPropList.stream().filter(a -> StringUtils.equalsIgnoreCase(a, field.getName())).findFirst();
                isOk = item.isPresent();
            }
            if (isOk) {
                field.setAccessible(true);
                Object val = null;
                try {
                    val = field.get(bean);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (val == null) {
                    if (field.getType() == BigDecimal.class) {
                        val = BigDecimal.ZERO;
                    } else {
                        return;
                    }
                }
                if (field.isAnnotationPresent(EmbeddedId.class)) {
                    Map<String, Object> map = getAllFields(val, targetPropList);
                    ret.putAll(map);
                    return;
                }
                if (val instanceof Enum<?>) {
                    Enum<?> val1 = (Enum<?>) val;
                    val = val1.ordinal();
                }
                ret.put((item != null && item.isPresent()) ? item.get() : field.getName(), val);
            }

        });
        return ret;
    }

    /**
     * 反射获取对象属性，包括上级，不忽略任何值
     *
     * @return
     */
    public static Map<String, Object> getAllFieldsForBatchAdd(Object bean) {
        Map<String, Object> ret = new HashMap<>();
        reflectAllField(bean.getClass(), field -> {
            if (ret.containsKey(field.getName())) {
                return;
            }
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            //忽略静态属性
            if (isStatic) {
                return;
            }
            field.setAccessible(true);
            Object val = null;
            try {
                val = field.get(bean);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (val == null) {
                if (val.getClass() == BigDecimal.class) {
                    val = BigDecimal.ZERO;
                }
            }
            if (field.isAnnotationPresent(EmbeddedId.class)) {
                assert val != null;
                Map<String, Object> map = getAllFieldsForBatchAdd(val);
                ret.putAll(map);
                return;
            }
            if (val instanceof Enum<?>) {
                //枚举对象取ordinal
                Enum<?> anEnum = (Enum<?>) val;
                val = anEnum.ordinal();
            }
            ret.put(field.getName(), val);
        });
        return ret;
    }

    public static Map<String, Object> getAllFields(Object bean) {
        return getAllFields(bean, null);
    }

    /**
     * 根据反射和传入字段名称获取对应class中对应该字段的值
     *
     * @return
     */
    public static Object getValue(Object o, String name) {
        Class<?> clazz = o.getClass();
        Field field = getField(clazz, name);
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (Exception e) {
            LogUtil.err("", e);
        }
        return null;
    }
}
