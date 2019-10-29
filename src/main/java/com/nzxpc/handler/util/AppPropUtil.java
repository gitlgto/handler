package com.nzxpc.handler.util;

import org.apache.commons.lang3.StringUtils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class AppPropUtil {
    private static ResourceBundle APP_PROP_RESOURCE = null;

    static {
        try {
            APP_PROP_RESOURCE = ResourceBundle.getBundle("application");
        } catch (MissingResourceException e) {
            LogUtil.info("缺少配置文件");
        }

    }

    public static String get(String key) {
        if (APP_PROP_RESOURCE == null || !APP_PROP_RESOURCE.containsKey(key)) {
            return null;
        }
        return StringUtils.trim(APP_PROP_RESOURCE.getString(key));
    }
}
