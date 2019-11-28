package com.nzxpc.handler.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

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
    /**
     * desc 判断是否是本地地址或内网地址，比如127.0.0.1、localhost、172.17.80.73都会返回true
     **/
    public static boolean isLocal(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isSiteLocalAddress();
        } catch (Exception e) {
            LogUtil.err(e);
            return false;
        }
    }
}
