package org.anyline.qq.open.util;

import org.anyline.entity.DataRow;
import org.anyline.qq.mp.util.QQMPConfig;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.qq.open.load.bean")
public class QQOpenBean implements InitializingBean {

    @Value("${anyline.qq.open.app:}")
    public String APP_ID 					; // AppID(应用ID)
    @Value("${anyline.qq.open.key:}")
    public String APP_KEY 					; // APPKEY(应用密钥)
    @Value("${anyline.qq.open.secret:}")
    public String APP_SECRET 				; // AppSecret(应用密钥)
    @Value("${anyline.qq.open.signType:}")
    public String SIGN_TYPE 				; // 签名加密方式
    @Value("${anyline.qq.open:token}")
    public String SERVER_TOKEN 				; // 服务号的配置token

    @Value("${anyline.qq.open.pay.secret:}")
    public String PAY_API_SECRET 			; // 商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
    @Value("${anyline.qq.open.pay.mch:}")
    public String PAY_MCH_ID 				; // 商家号
    @Value("${anyline.qq.open.pay.notify:}")
    public String PAY_NOTIFY_URL 			; // 支付统一接口的回调action
    @Value("${anyline.qq.open.pay.callback:}")
    public String PAY_CALLBACK_URL 			; // 支付成功支付后跳转的地址
    @Value("${anyline.qq.open.pay.key:}")
    public String PAY_KEY_STORE_FILE 		; // 支付证书存放路径地址

    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = BasicUtil.evl(APP_ID, QQMPConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID", BasicUtil.evl(APP_ID, QQOpenConfig.DEFAULT_APP_ID));
        row.put("APP_KEY", BasicUtil.evl(APP_KEY, QQOpenConfig.DEFAULT_APP_KEY));
        row.put("APP_SECRET", BasicUtil.evl(APP_SECRET, QQOpenConfig.DEFAULT_APP_SECRET));
        row.put("SIGN_TYPE", BasicUtil.evl(SIGN_TYPE, QQOpenConfig.DEFAULT_SIGN_TYPE));
        row.put("SERVER_TOKEN", BasicUtil.evl(SERVER_TOKEN, QQOpenConfig.DEFAULT_SERVER_TOKEN));
        row.put("PAY_API_SECRET", BasicUtil.evl(PAY_API_SECRET, QQOpenConfig.DEFAULT_PAY_API_SECRET));
        row.put("PAY_MCH_ID", BasicUtil.evl(PAY_MCH_ID, QQOpenConfig.DEFAULT_PAY_MCH_ID));
        row.put("PAY_NOTIFY_URL", BasicUtil.evl(PAY_NOTIFY_URL, QQOpenConfig.DEFAULT_PAY_NOTIFY_URL));
        row.put("PAY_CALLBACK_URL", BasicUtil.evl(PAY_CALLBACK_URL, QQOpenConfig.DEFAULT_PAY_CALLBACK_URL));
        row.put("PAY_KEY_STORE_FILE", BasicUtil.evl(PAY_KEY_STORE_FILE, QQOpenConfig.DEFAULT_PAY_KEY_STORE_FILE));

        QQOpenConfig.register(row);
    }

    @Bean("anyline.qq.open.init.bean")
    public QQOpenUtil instance(){
        return QQOpenUtil.getInstance();
    }
}
