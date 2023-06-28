package org.anyline.boot.amap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration("anyline.boot.amap")
@ConfigurationProperties(prefix = "anyline.amap")
public class AmapProperty {

    private String host     ;
    private String key		;
    private String secret 	;
    private String table 	;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
