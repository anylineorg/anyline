/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.wechat.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.wechat.mp.util.WechatMPConfig;
import org.anyline.wechat.mp.util.WechatMPUtil;
import org.anyline.wechat.open.util.WechatOpenConfig;
import org.anyline.wechat.open.util.WechatOpenUtil;
import org.anyline.wechat.program.WechatProgramConfig;
import org.anyline.wechat.program.WechatProgramUtil;
import org.anyline.wechat.wap.util.WechatWapConfig;
import org.anyline.wechat.wap.util.WechatWapUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.wechat.load.bean")
public class WechatBean implements InitializingBean {

    @Value("${anyline.wechat.app:}")
    private String APP_ID 						 	; // AppID(应用ID)
    @Value("${anyline.wechat.secret:}")
    private String APP_SECRET 					 	; // AppSecret(应用密钥)
    @Value("${anyline.wechat.signType:}")
    private String SIGN_TYPE 					 	; // 签名加密方式
    @Value("${anyline.wechat.token:}")
    private String SERVER_TOKEN 					; // 服务号的配置token
    @Value("${anyline.wechat.redirect:}")
    private String OAUTH_REDIRECT_URL 				; // oauth2授权时回调action
    @Value("${anyline.wechat.server:}")
    private String WEB_SERVER 					 	;
    @Value("${anyline.wechat.tokenServer:}")
    private String ACCESS_TOKEN_SERVER			 	;
    @Value("${anyline.wechat.whitelist:}")
    private String SERVER_WHITELIST			   		; // 白名单IP(如果设置了并且当前服务器不在白名单内,则跳过需要白名单才能调用的接口)


    @Override
    public void afterPropertiesSet()  {
        APP_ID = BasicUtil.evl(APP_ID, WechatConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID", BasicUtil.evl(APP_ID, WechatConfig.DEFAULT_APP_ID));
        row.put("APP_SECRET", BasicUtil.evl(APP_SECRET, WechatConfig.DEFAULT_APP_SECRET));
        row.put("SIGN_TYPE", BasicUtil.evl(SIGN_TYPE, WechatConfig.DEFAULT_SIGN_TYPE));
        row.put("SERVER_TOKEN", BasicUtil.evl(SERVER_TOKEN, WechatConfig.DEFAULT_SERVER_TOKEN));
        row.put("OAUTH_REDIRECT_URL", BasicUtil.evl(OAUTH_REDIRECT_URL, WechatConfig.DEFAULT_OAUTH_REDIRECT_URL));
        row.put("WEB_SERVER", BasicUtil.evl(WEB_SERVER, WechatConfig.DEFAULT_WEB_SERVER));
        row.put("ACCESS_TOKEN_SERVER", BasicUtil.evl(ACCESS_TOKEN_SERVER, WechatConfig.DEFAULT_ACCESS_TOKEN_SERVER));
        row.put("SERVER_WHITELIST", BasicUtil.evl(SERVER_WHITELIST, WechatConfig.DEFAULT_SERVER_WHITELIST));

        WechatMPConfig.register(row);
        WechatOpenConfig.register(row);
        WechatProgramConfig.register(row);
        WechatWapConfig.register(row);
    }

    @Bean("anyline.wechat.mp.init.util")
    public WechatMPUtil instanceMp(){
        return WechatMPUtil.getInstance();
    }
    @Bean("anyline.wechat.open.init.util")
    public WechatOpenUtil instanceOpen(){
        return WechatOpenUtil.getInstance();
    }
    @Bean("anyline.wechat.program.init.util")
    public WechatProgramUtil instanceProgram(){
        return WechatProgramUtil.getInstance();
    }
    @Bean("anyline.wechat.wap.init.util")
    public WechatWapUtil instanceWap(){
        return WechatWapUtil.getInstance();
    }
}
