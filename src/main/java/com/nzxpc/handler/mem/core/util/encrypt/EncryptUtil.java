package com.nzxpc.handler.mem.core.util.encrypt;

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 加密/解密工具
 */
public class EncryptUtil {

    //采用某种算法加密后的密钥
    private static SecretKeySpec secretKey;

    //byte数组定义的密钥
    private static byte[] key;

    private static void setKey(String key1) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest sha = null;
        key = key1.getBytes("UTF-8");
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        secretKey = new SecretKeySpec(key, "AES");
    }

}
