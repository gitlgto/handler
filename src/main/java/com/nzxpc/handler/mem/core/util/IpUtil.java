package com.nzxpc.handler.mem.core.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class IpUtil {
    private static String dealIp(String ip) {
        if (ip != null) {
            int index = ip.indexOf(",");
            if (index > 0) {
                ip = ip.substring(0, index);
            }
        }
        return ip;
    }

    private static String _getIp(HttpServletRequest request) {
        String ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return dealIp(ip);
    }

    public static String getIp(HttpServletRequest request) {
        return _getIp(request);
    }
}
