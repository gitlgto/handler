package com.nzxpc.handler;

import org.springframework.context.ApplicationContext;

/**
 * 手动获取application和bean
 */
public class BeanContext {
    private static ApplicationContext APPLICATION_CONTEXT;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
    }
}
