package org.anyline.wechat.open.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.wechat.open.load.bean")
public class WechatOpenBean implements InitializingBean {

    @Value("${anyline.wechat.open.appId:}")
    private String APP_ID 							; // AppID(应用ID)
    @Value("${anyline.wechat.open.appSecret:}")
    private String APP_SECRET 					    ; // AppSecret(应用密钥)
    @Value("${anyline.wechat.open.signType:}")
    private String SIGN_TYPE 					    	; // 签名加密方式
    @Value("${anyline.wechat.open.serverToken:}")
    private String SERVER_TOKEN 					 	; // 服务号的配置token
    @Value("${anyline.wechat.open.oauthRedirectUrl:}")
    private String OAUTH_REDIRECT_URL 			    	; // oauth2授权时回调action
    @Value("${anyline.wechat.open.webServer:}")
    private String WEB_SERVER 					 	;
    @Value("${anyline.wechat.open.accessTokenServer:}")
    private String ACCESS_TOKEN_SERVER			    	;
    @Value("${anyline.wechat.open.serverWhitelist:}")
    private String SERVER_WHITELIST			   		; // 白名单IP(如果设置了并且当前服务器不在白名单内,则跳过需要白名单才能调用的接口)

    @Override
    public void afterPropertiesSet() {
        APP_ID = BasicUtil.evl(APP_ID, WechatOpenConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID", BasicUtil.evl(APP_ID, WechatOpenConfig.DEFAULT_APP_ID));
        row.put("APP_SECRET", BasicUtil.evl(APP_SECRET, WechatOpenConfig.DEFAULT_APP_SECRET));
        row.put("SIGN_TYPE", BasicUtil.evl(SIGN_TYPE, WechatOpenConfig.DEFAULT_SIGN_TYPE));
        row.put("SERVER_TOKEN", BasicUtil.evl(SERVER_TOKEN, WechatOpenConfig.DEFAULT_SERVER_TOKEN));
        row.put("OAUTH_REDIRECT_URL", BasicUtil.evl(OAUTH_REDIRECT_URL, WechatOpenConfig.DEFAULT_OAUTH_REDIRECT_URL));
        row.put("WEB_SERVER", BasicUtil.evl(WEB_SERVER, WechatOpenConfig.DEFAULT_WEB_SERVER));
        row.put("ACCESS_TOKEN_SERVER", BasicUtil.evl(ACCESS_TOKEN_SERVER, WechatOpenConfig.DEFAULT_ACCESS_TOKEN_SERVER));
        row.put("SERVER_WHITELIST", BasicUtil.evl(SERVER_WHITELIST, WechatOpenConfig.DEFAULT_SERVER_WHITELIST));
        WechatOpenConfig.register(row);
    }

    @Bean("anyline.wechat.open.init.util")
    public WechatOpenUtil instance(){
        return WechatOpenUtil.getInstance();
    }
}
