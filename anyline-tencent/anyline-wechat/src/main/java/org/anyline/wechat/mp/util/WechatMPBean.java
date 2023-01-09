package org.anyline.wechat.mp.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.wechat.mp.load.bean")
public class WechatMPBean implements InitializingBean {

    @Value("${anyline.wechat.mp.appId:}")
    private String APP_ID 							    ; // AppID(应用ID)
    @Value("${anyline.wechat.mp.appSecret:}")
    private String APP_SECRET 					        ; // AppSecret(应用密钥)
    @Value("${anyline.wechat.mp.signType:}")
    private String SIGN_TYPE 					    	; // 签名加密方式
    @Value("${anyline.wechat.mp.serverToken:}")
    private String SERVER_TOKEN 					 	; // 服务号的配置token
    @Value("${anyline.wechat.mp.oauthRedirectUrl:}")
    private String OAUTH_REDIRECT_URL 			    	; // oauth2授权时回调action
    @Value("${anyline.wechat.mp.webServer:}")
    private String WEB_SERVER 					 	    ;
    @Value("${anyline.wechat.mp.accessTokenServer:}")
    private String ACCESS_TOKEN_SERVER			    	;
    @Value("${anyline.wechat.mp.serverWhitelist:}")
    private String SERVER_WHITELIST			   	    	; // 白名单IP(如果设置了并且当前服务器不在白名单内,则跳过需要白名单才能调用的接口)

    @Override
    public void afterPropertiesSet()  {
        APP_ID = BasicUtil.evl(APP_ID, WechatMPConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID", BasicUtil.evl(APP_ID, WechatMPConfig.DEFAULT_APP_ID));
        row.put("APP_SECRET", BasicUtil.evl(APP_SECRET, WechatMPConfig.DEFAULT_APP_SECRET));
        row.put("SIGN_TYPE", BasicUtil.evl(SIGN_TYPE, WechatMPConfig.DEFAULT_SIGN_TYPE));
        row.put("SERVER_TOKEN", BasicUtil.evl(SERVER_TOKEN, WechatMPConfig.DEFAULT_SERVER_TOKEN));
        row.put("OAUTH_REDIRECT_URL", BasicUtil.evl(OAUTH_REDIRECT_URL, WechatMPConfig.DEFAULT_OAUTH_REDIRECT_URL));
        row.put("WEB_SERVER", BasicUtil.evl(WEB_SERVER, WechatMPConfig.DEFAULT_WEB_SERVER));
        row.put("ACCESS_TOKEN_SERVER", BasicUtil.evl(ACCESS_TOKEN_SERVER, WechatMPConfig.DEFAULT_ACCESS_TOKEN_SERVER));
        row.put("SERVER_WHITELIST", BasicUtil.evl(SERVER_WHITELIST, WechatMPConfig.DEFAULT_SERVER_WHITELIST));
        WechatMPConfig.register(row);
    }

    @Bean("anyline.wechat.mp.init.util")
    public WechatMPUtil instance(){
        return WechatMPUtil.getInstance();
    }
}
