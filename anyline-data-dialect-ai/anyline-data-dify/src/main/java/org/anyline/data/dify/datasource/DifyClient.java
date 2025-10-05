package org.anyline.data.dify.datasource;

public class DifyClient {
    private String host;
    private String secret;

    public DifyClient(){

    }
    public DifyClient(String host, String key) {
        this.host = host;
        this.secret = key;
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
