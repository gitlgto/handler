package com.nzxpc.handler.mem.core.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Set;
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
}
