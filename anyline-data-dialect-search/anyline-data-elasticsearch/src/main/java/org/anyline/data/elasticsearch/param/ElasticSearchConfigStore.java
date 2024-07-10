package org.anyline.data.elasticsearch.param;

import org.anyline.data.param.init.DefaultConfigStore;

public class ElasticSearchConfigStore extends DefaultConfigStore {
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
