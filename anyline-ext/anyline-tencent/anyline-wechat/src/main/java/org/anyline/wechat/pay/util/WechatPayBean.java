package org.anyline.wechat.pay.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.wechat.pay.load.bean")
public class WechatPayBean implements InitializingBean {
    @Value("${anyline.wechat.pay.mchId:}")
    public String MCH_ID 					 	; // 商户号
    @Value("${anyline.wechat.pay.spMchId:}")
    public String SP_MCH_ID 					 ; // 服务商商户号(服务商模式)
    @Value("${anyline.wechat.pay.subMchId:}")
    public String SUB_MCH_ID 				 	; // 子商户商户号(服务商模式)
    @Value("${anyline.wechat.pay.apiSecret:}")
    public String API_SECRET 				 	; // 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->API密钥设置
    @Value("${anyline.wechat.pay.apiSecret3:}")
    public String API_SECRET3				 	; // 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->APIv3密钥设置
    @Value("${anyline.wechat.pay.mchPrivateSecretFile:}")
    public String MCH_PRIVATE_SECRET_FILE	 	; // 商户API私钥(保存在apiclient_key.pem也可以通过p12导出)
    @Value("${anyline.wechat.pay.certificateSerial:}")
    public String CERTIFICATE_SERIAL         	; // 证书序号 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->API证书-->查看证书
    @Value("${anyline.wechat.pay.keyStoreFile:}")
    public String KEY_STORE_FILE 			 	; // 证书文件
    @Value("${anyline.wechat.pay.keyStorePassword:}")
    public String KEY_STORE_PASSWORD 		 	; // 证书密码
    @Value("${anyline.wechat.pay.notifyUrl:}")
    public String NOTIFY_URL				 	; // 微信支付统一接口的回调action
    @Value("${anyline.wechat.pay.callbackUrl:}")
    public String CALLBACK_URL 				 	; // 微信支付成功支付后跳转的地址
    @Value("${anyline.wechat.pay.bankRsaPublicKeyFile:}")
    public String BANK_RSA_PUBLIC_KEY_FILE 	 	;

    @Override
    public void afterPropertiesSet()  {
        MCH_ID = BasicUtil.evl(MCH_ID, WechatPayConfig.DEFAULT_MCH_ID);
        if(BasicUtil.isEmpty(MCH_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("MCH_ID", BasicUtil.evl(MCH_ID, WechatPayConfig.DEFAULT_MCH_ID));
        row.put("SP_MCH_ID", BasicUtil.evl(SP_MCH_ID, WechatPayConfig.DEFAULT_SP_MCH_ID));
        row.put("SUB_MCH_ID", BasicUtil.evl(SUB_MCH_ID, WechatPayConfig.DEFAULT_SUB_MCH_ID));
        row.put("API_SECRET", BasicUtil.evl(API_SECRET, WechatPayConfig.DEFAULT_API_SECRET));
        row.put("API_SECRET_V3", BasicUtil.evl(API_SECRET3, WechatPayConfig.DEFAULT_API_SECRET3));
        row.put("MCH_PRIVATE_SECRET_FILE", BasicUtil.evl(MCH_PRIVATE_SECRET_FILE, WechatPayConfig.DEFAULT_MCH_PRIVATE_SECRET_FILE));
        row.put("CERTIFICATE_SERIAL", BasicUtil.evl(CERTIFICATE_SERIAL, WechatPayConfig.DEFAULT_CERTIFICATE_SERIAL));
        row.put("KEY_STORE_FILE", BasicUtil.evl(KEY_STORE_FILE, WechatPayConfig.DEFAULT_KEY_STORE_FILE));
        row.put("KEY_STORE_PASSWORD", BasicUtil.evl(KEY_STORE_PASSWORD, WechatPayConfig.DEFAULT_KEY_STORE_PASSWORD));
        row.put("NOTIFY_URL", BasicUtil.evl(NOTIFY_URL, WechatPayConfig.DEFAULT_NOTIFY_URL));
        row.put("CALLBACK_URL", BasicUtil.evl(CALLBACK_URL, WechatPayConfig.DEFAULT_CALLBACK_URL));
        row.put("BANK_RSA_PUBLIC_KEY_FILE", BasicUtil.evl(BANK_RSA_PUBLIC_KEY_FILE, WechatPayConfig.DEFAULT_BANK_RSA_PUBLIC_KEY_FILE));
        WechatPayConfig.register(row);
    }

    @Bean("anyline.wechat.pay.init.util")
    public WechatPayUtil instance(){
        return WechatPayUtil.getInstance();
    }
}
