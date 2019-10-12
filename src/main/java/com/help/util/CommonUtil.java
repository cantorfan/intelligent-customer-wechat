package com.help.util;

import java.util.Map;

/**
 * 通用工具类
 *
 * @author JhonGuo
 * @date 2019/6/27 9:48
 */
public class CommonUtil {

    /**
     * 生成sessionId
     *
     * @param length sessionId长度
     * @return 指定长度的sessionId
     * @author JhonGuo
     */
    public static String generateSessionId(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append((char)(int)(Math.random()*26+97));
        }
        return stringBuilder.toString();
    }

    /**
     * 生成get参数
     *
     * @param map 需要被转换的map
     * @return get请求参数
     * @author JhonGuo
     */
    public static String generateGetParams(Map<String, Object> map) {
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (0 == count) {
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            } else {
                stringBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            count ++;
        }
        return stringBuilder.toString();
    }

}
