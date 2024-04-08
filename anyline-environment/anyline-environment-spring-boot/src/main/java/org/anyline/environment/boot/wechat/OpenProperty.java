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

package org.anyline.environment.boot.wechat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.environment.boot.tenant.wechat.open")
@ConfigurationProperties(prefix = "anyline.wechat.open")
public class OpenProperty {
    public String appId 							; // AppID(应用ID)
    public String appSecret 					    ; // AppSecret(应用密钥)
    public String signType 					    	; // 签名加密方式
    public String serverToken 					 	; // 服务号的配置token
    public String oauthRedirectUrl 			    	; // oauth2授权时回调action
    public String webServer 					 	;
    public String accessTokenServer			    	;
    public String serverWhitelist			   		; // 白名单IP(如果设置了并且当前服务器不在白名单内, 则跳过需要白名单才能调用的接口)

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getServerToken() {
        return serverToken;
    }

    public void setServerToken(String serverToken) {
        this.serverToken = serverToken;
    }

    public String getOauthRedirectUrl() {
        return oauthRedirectUrl;
    }

    public void setOauthRedirectUrl(String oauthRedirectUrl) {
        this.oauthRedirectUrl = oauthRedirectUrl;
    }

    public String getWebServer() {
        return webServer;
    }

    public void setWebServer(String webServer) {
        this.webServer = webServer;
    }

    public String getAccessTokenServer() {
        return accessTokenServer;
    }

    public void setAccessTokenServer(String accessTokenServer) {
        this.accessTokenServer = accessTokenServer;
    }

    public String getServerWhitelist() {
        return serverWhitelist;
    }

    public void setServerWhitelist(String serverWhitelist) {
        this.serverWhitelist = serverWhitelist;
    }
}
