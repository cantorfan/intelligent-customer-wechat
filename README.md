# 智能客服
此服务是作为微信小程序与第三方客服的中转站，作为中间媒介，转发客户在微信发的消息并传给三方客服，三方客服处理后的答案发送给中转站，最后由中转站再转发给小程序。
---
## 基本流程
1. 按照[消息推送](https://developers.weixin.qq.com/miniprogram/dev/framework/server-ability/message-push.html)
官方文档中要求编写签名校验接口并启动服务后在小程序后后管中配置请求url、token等信息进行校验。  
2. 编写消息处理接口，由服务端接收微信客服发来的消息，按照如下流程进行处理：   
2.1 若小程序后管设置消息为加密形式，则需要先对消息进行解密，可参照[加解密demo](https://res.wx.qq.com/op_res/-serEQ6xSDVIjfoOHcX78T1JAYX-pM_fghzfiNYoD8uHVd3fOeC0PC_pvlg4-kmP)文档  
2.2 提取解密后的xml消息  
2.3 按照第三方客服规定的接口规范获取答案  
2.4 根据[auth.getAccessToken](https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/access-token/auth.getAccessToken.html)
文档获取accessToken
2.5 最后根据[发送消息](https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/customer-message/customerServiceMessage.send.html)
文档发送给小程序。
