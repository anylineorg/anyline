package org.anyline.boot.baidu;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.boot.baidu.map")
@ConfigurationProperties(prefix = "anyline.baidu.map")
public class MapProperty {
    private String ak;
    private String sk;

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }
}
