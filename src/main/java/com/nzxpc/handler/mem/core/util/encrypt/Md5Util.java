package com.nzxpc.handler.mem.core.util.encrypt;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;

public class Md5Util {
    /**
     * 获取十六进制字符串形式的MD5摘要
     */
    public static String md5Hex(String src) {
        return md5Hex(src, "UTF-8");
    }

    /**
     * 获取十六进制字符串形式的MD5摘要
     */
    public static String md5Hex(String src, String charset) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(src.getBytes(charset));
            return new String(new Hex().encode(bs));
        } catch (Exception e) {
            return null;
        }
    }
}
