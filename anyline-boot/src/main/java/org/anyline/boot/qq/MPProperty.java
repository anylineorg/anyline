package org.anyline.boot.qq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.boot.qq.mp")
@ConfigurationProperties(prefix = "anyline.qq.mp")
public class MPProperty {

    private String appId			 	; // AppID(应用ID)
    private String apiKey 			 	; // APPKEY(应用密钥)
    private String oauthRedirectUrl 	; // 登录成功回调URL

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getOauthRedirectUrl() {
        return oauthRedirectUrl;
    }

    public void setOauthRedirectUrl(String oauthRedirectUrl) {
        this.oauthRedirectUrl = oauthRedirectUrl;
    }
}
