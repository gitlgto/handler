package com.nzxpc.handler.mem.core.util;

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

    /**
     * 反射获取对象属性值，包括所有上级类，其中枚举取ordinal值
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

    public static Map<String, Object> getAllFields(Object bean) {
        return getAllFields(bean, null);
    }
}
