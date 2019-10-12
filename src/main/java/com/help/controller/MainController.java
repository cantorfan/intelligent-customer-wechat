package com.help.controller;

import com.help.service.IMainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 主要Controller
 *
 * @author JhonGuo
 * @date 2019/6/11 17:26
 */
@RestController
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private IMainService mainService;
    @Value("${weChat.applet.csyhj.appId}")
    private String csyhjAppId;
    @Value("${weChat.applet.csyhj.secret}")
    private String csyhjSecret;
    @Value("${weChat.applet.csyhj.messagePush.token}")
    private String csyhjToken;
    @Value("${weChat.applet.csyhj.messagePush.encodingAesKey}")
    private String csyhjEncodingAesKey;

    @Value("${weChat.applet.nxd.appId}")
    private String nxdAppId;
    @Value("${weChat.applet.nxd.secret}")
    private String nxdSecret;
    @Value("${weChat.applet.nxd.messagePush.token}")
    private String nxdToken;
    @Value("${weChat.applet.nxd.messagePush.encodingAesKey}")
    private String nxdEncodingAesKey;

    /**
     * 常熟银行+签名校验
     *
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echoStr 随机字符串
     * @return 校验结果
     * @author JhonGuo
     */
    @GetMapping("request-handle-csyhj")
    public String checkCsyhjSignature(@RequestParam("signature") String signature,
                                  @RequestParam("timestamp") String timestamp,
                                  @RequestParam("nonce") String nonce,
                                  @RequestParam("echostr") String echoStr) {
        if (mainService.checksSignature(signature, timestamp, nonce, csyhjToken)) {
            logger.info("签名校验成功");
            return echoStr;
        } else {
            logger.info("签名校验失败");
            return "";
        }
    }

    /**
     * 常熟银行+消息发送
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param openid 用户的openId
     * @param encryptType 加密类型
     * @param msgSignature 签名消息
     * @param xmlMessage xml消息
     * @throws Exception AES加密异常
     * @author JhonGuo
     */
    @PostMapping("request-handle-csyhj")
    public void handleMessage(@RequestParam("signature") String signature,
                              @RequestParam("timestamp") String timestamp,
                              @RequestParam("nonce") String nonce,
                              @RequestParam("openid") String openid,
                              @RequestParam("encrypt_type") String encryptType,
                              @RequestParam("msg_signature") String msgSignature,
                              @RequestBody String xmlMessage) throws Exception {
        mainService.handleMessage(signature, timestamp, nonce, openid, encryptType, msgSignature, xmlMessage, csyhjAppId, csyhjSecret, csyhjToken, csyhjEncodingAesKey);
    }

    /**
     * 侬享贷签名校验
     *
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echoStr 随机字符串
     * @return 校验结果
     * @author JhonGuo
     */
    @GetMapping("request-handle-nxd")
    public String checkNxdSignature(@RequestParam("signature") String signature,
                                  @RequestParam("timestamp") String timestamp,
                                  @RequestParam("nonce") String nonce,
                                  @RequestParam("echostr") String echoStr) {
        if (mainService.checksSignature(signature, timestamp, nonce, nxdToken)) {
            logger.info("签名校验成功");
            return echoStr;
        } else {
            logger.info("签名校验失败");
            return "";
        }
    }

    /**
     * 侬享贷消息发送
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param openid 用户的openId
     * @param encryptType 加密类型
     * @param msgSignature 签名消息
     * @param xmlMessage xml消息
     * @throws Exception AES加密异常
     * @author JhonGuo
     */
    @PostMapping("request-handle-nxd")
    public void handleNxdMessage(@RequestParam("signature") String signature,
                              @RequestParam("timestamp") String timestamp,
                              @RequestParam("nonce") String nonce,
                              @RequestParam("openid") String openid,
                              @RequestParam("encrypt_type") String encryptType,
                              @RequestParam("msg_signature") String msgSignature,
                              @RequestBody String xmlMessage) throws Exception {
        mainService.handleMessage(signature, timestamp, nonce, openid, encryptType, msgSignature, xmlMessage, nxdAppId, nxdSecret, nxdToken, nxdEncodingAesKey);
    }

}
