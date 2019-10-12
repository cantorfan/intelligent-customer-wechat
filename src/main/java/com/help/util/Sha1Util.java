package com.help.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * sha1加密工具类
 *
 * @author JhonGuo
 * @date 2019/6/12 10:13
 */
public class Sha1Util {

    /**
     * sha1加密
     *
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     * @author JhonGuo
     */
    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] digest = mdTemp.digest();
            int j = digest.length;
            char[] buf = new char[j * 2];
            int k = 0;
            for (byte byte0 : digest) {
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

}
