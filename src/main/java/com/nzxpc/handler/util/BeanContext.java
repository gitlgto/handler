package com.nzxpc.handler.util;

import org.springframework.context.ApplicationContext;

/**
 * 手动获取application和bean
 */
public class BeanContext {
    private static ApplicationContext APPLICATION_CONTEXT;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
    }

    public static <T> T getBean(Class<T> tClass) {
        return APPLICATION_CONTEXT.getBean(tClass);
    }
}
