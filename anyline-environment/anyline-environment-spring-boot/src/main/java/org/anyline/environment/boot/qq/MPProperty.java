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

@Configuration("anyline.environment.boot.qq.mp")
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
