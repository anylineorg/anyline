package org.anyline.boot.print;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "anyline.p10ss")
public class P10ssProperty {

    public String appId                   ;
    public String appSecret               ;
    public String type                    ; // 0:自用 1:开放
    public String accessTokenServer       ;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccessTokenServer() {
        return accessTokenServer;
    }

    public void setAccessTokenServer(String accessTokenServer) {
        this.accessTokenServer = accessTokenServer;
    }
}
