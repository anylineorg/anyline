package org.anyline.boot.qq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "anyline.qq.open")
public class OpenProperty {

    public String APP_ID 					; // AppID(应用ID)
    public String APP_KEY 					; // APPKEY(应用密钥)
    public String APP_SECRET 				; // AppSecret(应用密钥)
    public String SIGN_TYPE 				; // 签名加密方式
    public String SERVER_TOKEN 				; // 服务号的配置token

    public String PAY_API_SECRET 			; // 商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置

    public String PAY_MCH_ID 				; // 商家号
    public String PAY_NOTIFY_URL 			; // 支付统一接口的回调action
    public String PAY_CALLBACK_URL 			; // 支付成功支付后跳转的地址
    public String PAY_KEY_STORE_FILE 		; // 支付证书存放路径地址
}
