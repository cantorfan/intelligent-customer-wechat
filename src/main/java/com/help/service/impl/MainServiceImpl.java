package com.help.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.help.constant.ThreePartConstant;
import com.help.constant.WeChatConstant;
import com.help.demo.WXBizMsgCrypt;
import com.help.service.IMainService;
import com.help.util.CommonUtil;
import com.help.util.HttpUtil;
import com.help.util.Md5Util;
import com.help.util.Sha1Util;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 主要Service实现
 *
 * @author JhonGuo
 * @date 2019/6/12 9:45
 */
@Service
public class MainServiceImpl implements IMainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainServiceImpl.class);

    @Value("${weChat.applet.accessTokenUrl}")
    private String accessTokenUrl;
    @Value("${weChat.applet.messageSendUrl}")
    private String messageSendUrl;
    @Value("${threePart.public.testUrl}")
    private String threePartUrl;
    @Value("${threePart.public.publicKey}")
    private String publicKey;
    @Value("${threePart.public.privateKey}")
    private String privateKey;

    /**
     * 签名校验
     * 1. 将token,timestamp,nonce按照字典顺序排序
     * 2. 将参数拼接成字符串进行sha1加密
     * 3. 将密文与传过来的加密签名进行比较，返回校验结果
     *
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param token     消息校验token
     * @return 校验结果
     * @author JhonGuo
     */
    @Override
    public boolean checksSignature(String signature, String timestamp, String nonce, String token) {
        // 先按字典顺序排序
        String[] params = {token, timestamp, nonce};
        Arrays.sort(params);
        LOGGER.info("字典排序完成" + Arrays.toString(params));
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : params) {
            stringBuilder.append(s);
        }
        // 进行sha1加密
        String sha1 = Sha1Util.getSha1(stringBuilder.toString());
        LOGGER.info("加密结果为" + sha1);
        if (null != sha1) {
            return sha1.equals(signature);
        } else {
            LOGGER.error("加密失败");
            return false;
        }
    }

    /**
     * 消息处理
     *
     * @param signature      微信加密签名
     * @param timestamp      时间戳
     * @param nonce          随机数
     * @param openid         用户的openId
     * @param encryptType    加密类型
     * @param msgSignature   签名消息
     * @param xmlMessage     xml消息
     * @param appId          小程序appId
     * @param secret         秘钥
     * @param token          消息校验token
     * @param encodingAesKey 消息加密秘钥
     * @throws Exception AES加密异常
     * @author JhonGuo
     */
    @Override
    public void handleMessage(String signature, String timestamp, String nonce, String openid, String encryptType,
                              String msgSignature, String xmlMessage, String appId, String secret, String token, String encodingAesKey) throws Exception {
        StringBuilder replyMessage = new StringBuilder();
        // 消息验证并解密xml消息
        WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
        String xmlString = pc.decryptMsg(msgSignature, timestamp, nonce, xmlMessage);
        LOGGER.info("解密后的消息内容为：" + xmlString);
        // 获取xml中的字符信息，包括ToUserName,FromUserName,CreateTime,MsgType,Content,MsgId
        Map<String, String> xmlInfo = getXmlInfo(xmlString);
        // 提取客户提的问题
        String question = xmlInfo.get("Content");
        String account = xmlInfo.get("FromUserName");
        String msgType = xmlInfo.get("MsgType");
        String event = xmlInfo.get("Event");
        // 处理消息为event类型的消息
        if (WeChatConstant.MSG_TYPE_EVENT.equals(msgType)) {
            // 用户进入客服的标志
            if (WeChatConstant.EVENT_ENTER.equals(event)) {
                replyMessage.append("智能客服\"小燕\"欢迎您！请直接输入问题，小燕7x24小时为您解答。");
            }
            // 处理消息为txt类型的消息
        } else if (WeChatConstant.MSG_TYPE_TEXT.equals(msgType)) {
            // 向第三方客服请求答案
            Map<String, Object> requestMap = new HashMap<>(16);
            requestMap.put("question", question);
            requestMap.put("pubkey", publicKey);
            requestMap.put("account", account);
            requestMap.put("sessionId", CommonUtil.generateSessionId(8));
            requestMap.put("time_stamp", System.currentTimeMillis() / 1000);
            // 生成sign，其中question和pubkey不需要urlencode
            String sign = generateSign(requestMap);
            requestMap.put("sign", sign);
            // 传参时question和pubkey需要进行urlencode
            requestMap.put("question", URLEncoder.encode(question, StandardCharsets.UTF_8.toString()));
            requestMap.put("pubkey", URLEncoder.encode(publicKey, StandardCharsets.UTF_8.toString()));
            LOGGER.info("请求的参数为：" + requestMap);
            // 向第三方智能客服获取回答
            String threePartResponse = HttpUtil.doGet(threePartUrl + "?" + CommonUtil.generateGetParams(requestMap));
            LOGGER.info("三方客服回包信息为" + threePartResponse);
            // 提取信息
            JSONObject jsonObject = JSONObject.parseObject(threePartResponse);
            String msg = jsonObject.getString("msg");
            Integer status = jsonObject.getInteger("status");
            Integer type = jsonObject.getInteger("type");
            // 成功回包标志
            boolean flag = ThreePartConstant.MESSAGE_SUCCESS.equals(msg) && ThreePartConstant.STATUS_SUCCESS == status
                    && (ThreePartConstant.TYPE_ANSWER_ONE == type || ThreePartConstant.TYPE_ANSWER_MANY == type);
            if (flag) {
                JSONArray info = jsonObject.getJSONArray("info");
                int size = info.size();
                for (int i = 0; i < size; i++) {
                    replyMessage.append(jsonObject.getJSONArray("info").getJSONObject(i).getString("answer"));
                    // 多个答案需要换行
                    if (size > 1 && i != size - 1) {
                        replyMessage.append("\n\n");
                    }
                }
            } else {
                replyMessage.append("小燕好像没有听懂您在问什么，暂时无法回答您的问题哦");
                LOGGER.info("错误码为" + status + "，原因是" + msg);
            }
        }
        // 获取accessToken
        String accessToken = getAccessToken(appId, secret);
        LOGGER.info("获取到的token为" + accessToken);
        // 向微信用户发送消息
        String response = sendMessage(openid, replyMessage.toString(), accessToken);
        LOGGER.info("微信返回结果为" + response);
        // 处理错误码为45047的情况 out of response count limit hint
        JSONObject jsonObject = JSONObject.parseObject(response);
        String errCode = jsonObject.getString("errcode");
        // 当错误代码是45047时，再次发送消息
        if (WeChatConstant.CODE_ERROR_UPPER_LIMIT.equals(errCode)) {
            sendMessage(openid, replyMessage.toString(), accessToken);
        }
    }

    /**
     * 获取token(微信)
     *
     * @param appId  小程序appId
     * @param secret 秘钥
     * @return 访问令牌
     * @author JhonGuo
     */
    private String getAccessToken(String appId, String secret) {
        String url = accessTokenUrl + "?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        String accessToken = HttpUtil.doGet(url);
        JSONObject jsonObject = JSONObject.parseObject(accessToken);
        return String.valueOf(jsonObject.get("access_token"));
    }

    /**
     * 发送消息给用户
     *
     * @param openid      用户的openId
     * @param message     消息
     * @param accessToken 访问令牌
     * @return 发送结果
     * @author JhonGuo
     */
    private String sendMessage(String openid, String message, String accessToken) throws Exception {
        Map<String, Object> contentMap = new HashMap<>(16);
        contentMap.put("content", message);
        Map<String, Object> messageMap = new HashMap<>(16);
        messageMap.put("touser", openid);
        messageMap.put("msgtype", "text");
        messageMap.put("text", contentMap);
        return HttpUtil.doPost(messageSendUrl + "?access_token=" + accessToken, JSON.toJSONString(messageMap));
    }

    /**
     * 获取xml信息（第二层节点信息）
     *
     * @param xmlString xml字符串
     * @return xml中的键值对信息
     * @author JhonGuo
     */
    private Map<String, String> getXmlInfo(String xmlString) throws Exception {
        // 创建一个SAXReader对象
        SAXReader sax = new SAXReader();
        StringReader read = new StringReader(xmlString);
        // 获取document对象,如果文档无节点，则会抛出Exception提前结束
        Document document = sax.read(read);
        // 获取根节点
        Element root = document.getRootElement();
        // 获取根节点下面的所有子节点（不包过子节点的子节点）
        List<Element> list = root.elements();
        Map<String, String> map = new HashMap<>(16);
        // 遍历List的方法
        for (Element e : list) {
            map.put(e.getName(), e.getTextTrim());
        }
        return map;
    }

    /**
     * 生成sign
     * 1. 将参数按照key进行字典排序
     * 2. 将value按照排好的顺序拼接起来
     * 3. 拼接上秘钥
     * 4. 进行base64编码
     * 5. 最后进行md5加密，得到sign
     *
     * @param map 包含生成sign参数的map
     * @return sign
     * @author JhonGuo
     */
    private String generateSign(Map<String, Object> map) {
        // 将key按照字典顺序排序
        List<String> keyList = new ArrayList<>(map.keySet());
        Collections.sort(keyList);
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keyList) {
            stringBuilder.append(map.get(key));
        }
        // 拼接秘钥（私）
        stringBuilder.append(privateKey);
        // base64编码
        String base64String = Base64.getEncoder().encodeToString(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        // 进行md5加密
        return Md5Util.getMd5(base64String);
    }

}
