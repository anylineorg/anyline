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

@Configuration("anyline.environment.boot.tenant.wechat.pay")
@ConfigurationProperties(prefix = "anyline.wechat.pay")
public class PayProperty {

    public String mchId 					 	; // 商户号
    public String spMchId 					 	; // 服务商商户号(服务商模式)
    public String subMchId 				 	    ; // 子商户商户号(服务商模式)
    public String apiSecret 				 	; // 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->API密钥设置
    public String apiSecret3				 	; // 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->APIv3密钥设置
    public String mchPrivateSecretFile	 	    ; // 商户API私钥(保存在apiclient_key.pem也可以通过p12导出)
    public String certificateSerial         	; // 证书序号 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->API证书-->查看证书
    public String keyStoreFile 			 	    ; // 证书文件
    public String keyStorePassword 		 	    ; // 证书密码
    public String notifyUrl				 	    ; // 微信支付统一接口的回调action
    public String callbackUrl 				 	; // 微信支付成功支付后跳转的地址
    public String bankRsaPublicKeyFile 	 	    ;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getSpMchId() {
        return spMchId;
    }

    public void setSpMchId(String spMchId) {
        this.spMchId = spMchId;
    }

    public String getSubMchId() {
        return subMchId;
    }

    public void setSubMchId(String subMchId) {
        this.subMchId = subMchId;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getApiSecret3() {
        return apiSecret3;
    }

    public void setApiSecret3(String apiSecret3) {
        this.apiSecret3 = apiSecret3;
    }

    public String getMchPrivateSecretFile() {
        return mchPrivateSecretFile;
    }

    public void setMchPrivateSecretFile(String mchPrivateSecretFile) {
        this.mchPrivateSecretFile = mchPrivateSecretFile;
    }

    public String getCertificateSerial() {
        return certificateSerial;
    }

    public void setCertificateSerial(String certificateSerial) {
        this.certificateSerial = certificateSerial;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getBankRsaPublicKeyFile() {
        return bankRsaPublicKeyFile;
    }

    public void setBankRsaPublicKeyFile(String bankRsaPublicKeyFile) {
        this.bankRsaPublicKeyFile = bankRsaPublicKeyFile;
    }
}
