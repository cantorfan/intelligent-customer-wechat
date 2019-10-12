package com.help.service;

/**
 * 主要Service
 *
 * @author JhonGuo
 * @date 2019/6/12 9:39
 */
public interface IMainService {

    /**
     * 签名校验
     *
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param token 消息校验token
     * @return 校验结果
     * @author JhonGuo
     */
    boolean checksSignature(String signature, String timestamp, String nonce, String token);

    /**
     * 消息处理
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param openid 用户的openId
     * @param encryptType 加密类型
     * @param msgSignature 签名消息
     * @param xmlMessage xml消息
     * @param appId 小程序appId
     * @param secret 秘钥
     * @param token 消息校验token
     * @param encodingAesKey 消息加密秘钥
     * @throws Exception AES加密异常
     * @author JhonGuo
     */
    void handleMessage(String signature, String timestamp, String nonce, String openid,
                       String encryptType, String msgSignature, String xmlMessage, String appId,
                       String secret, String token, String encodingAesKey) throws Exception;

}
