package com.nzxpc.handler.mem.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;

public class LogUtil {
    public static boolean IsInfoLevel;

    static {
        String s = AppPropUtil.get("logging.level.root");
        IsInfoLevel = StringUtils.isBlank(s) || "info".equals(s);
    }

    public static void info(String msg) {
        if (IsInfoLevel) {
            LogFactory.getLog(new CurrentClassGetter().getCurrentClass()).info(msg);
        }
    }

    public static void err(Class<?> clazz, Throwable e) {
        LogFactory.getLog(clazz).error(e.getMessage(), e);
    }

    public static void err(String msg) {
        LogFactory.getLog(new CurrentClassGetter().getCurrentClass()).error(msg);
    }

    public static void err(String msg, Throwable e) {
        LogFactory.getLog(new CurrentClassGetter().getCurrentClass()).error(msg, e);
    }

    private static class CurrentClassGetter extends SecurityManager {
        public Class getCurrentClass() {
            Class<?>[] arr = getClassContext();
            int i = 2;
            for (i = 2; i < arr.length; i++) {
                if (arr[i] != LogUtil.class) {
                    break;
                }
            }
            return arr[i];
        }
    }
}
