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



package org.anyline.environment.boot.qq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.environment.boot.qq.open")
@ConfigurationProperties(prefix = "anyline.qq.open")
public class OpenProperty {

    public String appId 					; // AppID(应用ID)
    public String appKey 					; // APPKEY(应用密钥)
    public String appSecret 				; // AppSecret(应用密钥)
    public String signType 				    ; // 签名加密方式
    public String serverToken 				; // 服务号的配置token

    public String payApiSecret 			    ; // 商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置

    public String payMchId 			    	; // 商家号
    public String payNotifyUrl 		    	; // 支付统一接口的回调action
    public String payCallbackUrl 			; // 支付成功支付后跳转的地址
    public String payKeyStoreFile 	    	; // 支付证书存放路径地址

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
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

    public String getPayApiSecret() {
        return payApiSecret;
    }

    public void setPayApiSecret(String payApiSecret) {
        this.payApiSecret = payApiSecret;
    }

    public String getPayMchId() {
        return payMchId;
    }

    public void setPayMchId(String payMchId) {
        this.payMchId = payMchId;
    }

    public String getPayNotifyUrl() {
        return payNotifyUrl;
    }

    public void setPayNotifyUrl(String payNotifyUrl) {
        this.payNotifyUrl = payNotifyUrl;
    }

    public String getPayCallbackUrl() {
        return payCallbackUrl;
    }

    public void setPayCallbackUrl(String payCallbackUrl) {
        this.payCallbackUrl = payCallbackUrl;
    }

    public String getPayKeyStoreFile() {
        return payKeyStoreFile;
    }

    public void setPayKeyStoreFile(String payKeyStoreFile) {
        this.payKeyStoreFile = payKeyStoreFile;
    }
}
