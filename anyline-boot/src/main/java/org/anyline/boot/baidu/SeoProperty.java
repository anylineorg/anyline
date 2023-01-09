package org.anyline.boot.baidu;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.boot.baidu.seo")
@ConfigurationProperties(prefix = "anyline.baidu.seo")
public class SeoProperty {
    private String site;
    private String token;

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
